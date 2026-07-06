package ru.vlad.vlad_data_receiver.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.BundleEntity;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.entity.OperationalDayEntity;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.exceptions.StorageException;
import ru.vlad.vlad_data_receiver.saver.BundleSaver;
import ru.vlad.vlad_data_receiver.model.constants.BundleStatuses;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentAttribute;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleProcessingService {
    private final BundleSaver bundleSaver;
    private final DocumentCrudService documentCrudService;
    private final BundleCrudService bundleCrudService;
    private final UnloadingCrudService unloadingCrudService;
    private final OperationalDayCrudService operationalDayCrudService;
    private Cache<String, UnloadingEntity> unloadingCache;

    @PostConstruct
    protected void initCache() {
        unloadingCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public void processBundle(DocumentInputRequest documentInputRequest) {
        String unloadingRequestId = documentInputRequest.getID();
        LocalDateTime documentTimeStamp = documentInputRequest.getTimeStamp();
        LocalDate documentOperationalDayDate = documentTimeStamp.toLocalDate();

        // 4.1 Сервис вычитывает опердень выгрузки и считывает его статус из базы.
        // В случае, если статус = UNLOADING_RECEIVE_STOPPED (2), сервис прекращает обработку бандла и продолжает ожидать поступление других запросов
        OperationalDayEntity operationalDay = operationalDayCrudService.findByDate(documentOperationalDayDate);
        if (operationalDay.getStateId() == 2) {
            log.error("Для выгрузки для запроса с ID={} опердень находится в статусе UNLOADING_RECEIVE_STOPPED", unloadingRequestId);
            return;
        }

        String odDocType = documentInputRequest.getOdDocType();
        List<Document> documents = documentInputRequest.getDocument();

        // 4.2 Сервис проверяет по аттрибуту ID из принятого файла Document_Input_Request.xml наличие в кэше (срок хранения 1 минута)
        // записи о выгрузке с таким же значением unloading_request_id.
        UnloadingEntity unloading = unloadingCache.getIfPresent(unloadingRequestId);
        if (unloading != null) {
            log.info("Выгрузка для запроса с ID {} найдена в кэше. Используем её для текущего запроса", unloadingRequestId);
        } else {
            log.info("Выгрузка для запроса с ID={} не найдена в кэше, создаём новую", unloadingRequestId);
            try {
                DocumentCard documentCard = documents.get(0).getDocumentCard();
                UnloadingEntity newUnloading = UnloadingEntity.builder()
                        .unloadingRequestId(unloadingRequestId)
                        .sourceSystemCode(getSourceSystemCode(documentCard))
                        .date(documentTimeStamp)
                        .totalDocs(documentInputRequest.getTotalDocs())
                        .stateId(1L)
                        .operationalDayId(operationalDay.getId())
                        .departmentNumber(getDepartmentNumber(documentCard))
                        .docCategory(odDocType)
                        .build();
                unloading = unloadingCrudService.save(newUnloading);
                log.info("Новая выгрузка для запроса с ID={} успешно записана в БД", unloadingRequestId);
                unloadingCache.put(unloadingRequestId, unloading);
                // 4.3.1 При успешном создании выгрузки сервис записывает ее параметры в кэш и использует для создания бандла
            } catch (DbActionExecutionException e) {
                // 4.3.2. При неуспешном создании выгрузки (unique constraint violation-проверка в БД на существующую выгрузку)
                // сервис делает запрос к таблице unloading в БД по unloaing_request_id,
                // записывает параметры выгрузки в кэш и использует их для нового бандла
                log.warn("Конфликт уникальности при записи выгрузки для запроса с ID={} в БД. Запрашиваем существующую запись", unloadingRequestId);
                unloading = unloadingCrudService.findByUnloadingRequestId(unloadingRequestId);
                log.info("Выгрузка с ID {} найдена в БД после конфликта. Используем её для текущего запроса", unloadingRequestId);
                unloadingCache.put(unloadingRequestId, unloading);
            }
        }

        // 4.5 В случае, если статус выгрузки меньше нуля,
        // то сервис прекращает обработку бандла и возвращается слушать очередь системы источника
        if (unloading.getStateId() < 0) {
            log.error("У выгрузки с ID {} статус меньше 0, прекращаем обработку", unloadingRequestId);
            return;
        }

        // 5. Сервис создает запись в таблице bundle со статусом "Новый" (NEW_BUNDLE)
        BundleEntity bundleEntity = BundleEntity.builder()
                .status(BundleStatuses.NEW_BUNDLE.getStatus())
                .documentCount(unloading.getTotalDocs())
                .bundleNum(documentInputRequest.getBlockNum())
                .unloadingRequestId(unloading.getUnloadingRequestId())
                .type(2)
                .od_p(documentOperationalDayDate)
                .build();

        BundleEntity savedBundle = bundleCrudService.save(bundleEntity);
        Long bundleId = savedBundle.getId();

        // 6. В случае отсутствия ошибок сервис переходит к сохранению документов на диск и метаданных в БД (Use Case 03)
        log.info("Начало обработки бандла {} для выгрузки {}", bundleId, unloadingRequestId);

        // 1. Сервис записывает информацию из бандла в БД:
        //  1.1 Метаданные в БД: таблица document
        List<DocumentEntity> savedDocuments;
        try {
            savedDocuments = new ArrayList<>();

            for (Document document : documents) {
                DocumentEntity.DocumentEntityBuilder documentEntityBuilder = DocumentEntity.builder()
                        .bundleId(bundleId)
                        .unloadingRequestId(unloadingRequestId)
                        .number(String.valueOf(document.getSeqN()))
                        .timestamp(documentInputRequest.getTimeStamp())
                        .od_p(documentOperationalDayDate)
                        .format(document.getDocumentBody().getContent().get(0).getFormat());

                for (DocumentAttribute attr : document.getDocumentCard().getVariableAttribute()) {
                    String attributeCode = attr.getAttributeCode();
                    Object attributeValue = attr.getAttributeValue();

                    DocumentAttributeCodes documentAttributeCode = DocumentAttributeCodes.fromString(attributeCode);
                    if (documentAttributeCode == null) {
                        continue;
                    }

                    switch (documentAttributeCode) {
                        case DOC_TYPE -> documentEntityBuilder.doctypeCode(attributeValue.toString());
                        case DOC_NUMBER -> documentEntityBuilder.number(attributeValue.toString());
                        case DOC_ACCOUNT ->
                                documentEntityBuilder.departmentCode(Integer.parseInt(attributeValue.toString()));
                        case DOC_SUM -> documentEntityBuilder.docSum(new BigDecimal(attributeValue.toString()));
                        case DOC_SIGN_1 -> documentEntityBuilder.sign1(attributeValue.toString());
                        case DOC_SIGN_2 -> documentEntityBuilder.sign2(attributeValue.toString());
                        case DOC_SIGN_3 -> documentEntityBuilder.sign3(attributeValue.toString());
                    }
                }

                DocumentEntity entity = documentEntityBuilder.build();

                DocumentEntity savedEntity = documentCrudService.save(entity);
                savedDocuments.add(savedEntity);
            }
            log.info("Сохранено {} документов в БД", savedDocuments.size());
        } catch (DbActionExecutionException e) {
            setBundleStatus(savedBundle, BundleStatuses.SAVING_METADATA_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}", unloadingRequestId, e.getMessage(), e.getCause());
            return;
        }

        // 2. Сервис формирует ZIP файл c именем {id бандла}.zip, содержащий все декодированные из base64 файлы документов бандла.
        // 2.1 Имя файла каждого документа - {id документа}.{расширение}
        // 3. Сервис сохраняет в папке формата {YYYYMMDD}/{Тип документов} zip-файл со всеми документами бандла.
        try {
            bundleSaver.process(bundleId, savedDocuments, documents, odDocType);
        } catch (StorageException e) {
            // 4. В случае возникновения ошибок сохранения в CEPH:
            // - Сервис переводит бандл в статус "Ошибка сохранения на диске" (SAVING_FILER_ERROR) в БД
            // - Сервис возвращается к ожиданию сообщений по RESTу.
            setBundleStatus(savedBundle, BundleStatuses.SAVING_FILER_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}", unloadingRequestId, e.getMessage(), e.getCause());
            return;
        }

        // После сохранения всей информации бандла сервис меняет значение статуса бандл на "Сохранен" (BUNDLE_SAVED)
        setBundleStatus(savedBundle, BundleStatuses.BUNDLE_SAVED);
        log.info("Бандл для выгрузки с ID={} успешно сохранён", unloadingRequestId);
    }

    private void setBundleStatus(BundleEntity savedBundle, BundleStatuses bundleStatus) {
        savedBundle.setStatus(bundleStatus.getStatus());
        bundleCrudService.update(savedBundle);
    }

    private int getDepartmentNumber(DocumentCard documentCard) {
        return documentCard.getVariableAttribute().stream()
                .filter(attr -> DocumentAttributeCodes.DOC_ACCOUNT.getCode().equals(attr.getAttributeCode()))
                .findFirst()
                .map(attr -> Integer.parseInt(attr.getAttributeValue().toString()))
                .orElse(0);
    }

    private String getSourceSystemCode(DocumentCard documentCard) {
        return documentCard.getVariableAttribute().stream()
                .filter(attr -> DocumentAttributeCodes.DOC_SOURCE_SYSTEM.getCode().equals((attr.getAttributeCode())))
                .findFirst()
                .map(attr -> attr.getAttributeValue().toString())
                .orElse(null);
    }
}