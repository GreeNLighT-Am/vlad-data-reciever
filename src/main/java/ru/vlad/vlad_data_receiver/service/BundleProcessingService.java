package ru.vlad.vlad_data_receiver.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.BundleEntity;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.entity.OperationalDayEntity;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.exceptions.StorageException;
import ru.vlad.vlad_data_receiver.mappers.DocumentMapper;
import ru.vlad.vlad_data_receiver.model.constants.OdDocTypes;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingStatuses;
import ru.vlad.vlad_data_receiver.saver.BundleSaver;
import ru.vlad.vlad_data_receiver.model.constants.BundleStatuses;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;

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
    private final DocumentMapper documentMapper;
    private Cache<String, UnloadingEntity> unloadingCache;

    @PostConstruct
    protected void initCache() {
        unloadingCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Transactional
    public void processBundle(DocumentInputRequest documentInputRequest) {
        String unloadingRequestId = documentInputRequest.getID();
        List<Document> allDocumentsFromRequest = documentInputRequest.getDocument();
        DocumentCard documentCardOfFirstDocumentFromRequest = allDocumentsFromRequest.get(0).getDocumentCard();
        // В ходе приема бандла опердень выгрузки бандла определяем из аттрибута DocDate, а код подразделения (филиала) из DocAccount первого документа бандла.
        LocalDate documentOperationalDayDate = getOperationalDayDate(documentCardOfFirstDocumentFromRequest).toLocalDate();

        // 4.1 Сервис вычитывает опердень выгрузки и считывает его статус из базы.
        // В случае, если статус = UNLOADING_RECEIVE_STOPPED (2), сервис прекращает обработку бандла и продолжает ожидать поступление других запросов
        OperationalDayEntity operationalDay = operationalDayCrudService.findByDate(documentOperationalDayDate);
        if (operationalDay.getStateId() == 2) {
            log.error("Для выгрузки с ID={} опердень находится в статусе UNLOADING_RECEIVE_STOPPED", unloadingRequestId);
            return;
        }

        LocalDateTime documentTimeStamp = documentInputRequest.getTimeStamp();
        String odDocType = documentInputRequest.getOdDocType();

        // 4.2 Сервис проверяет по аттрибуту ID из принятого файла Document_Input_Request.xml наличие в кэше (срок хранения 1 минута)
        // записи о выгрузке с таким же значением unloading_request_id.
        UnloadingEntity unloading = unloadingCache.getIfPresent(unloadingRequestId);

        if (unloading != null) {
            log.info("Выгрузка для запроса с ID={} найдена в кэше. Используем её для текущего запроса", unloadingRequestId);
        } else {
            log.info("Выгрузка для запроса с ID={} не найдена в кэше, создаём новую", unloadingRequestId);

            int inserted = unloadingCrudService.insertIfNotExists(
                    unloadingRequestId,
                    getSourceSystemCode(documentCardOfFirstDocumentFromRequest),
                    documentTimeStamp,
                    documentInputRequest.getTotalDocs(),
                    UnloadingStatuses.GOOD_UNLOADING.getStatus(),
                    operationalDay.getId(),
                    getDepartmentNumber(documentCardOfFirstDocumentFromRequest),
                    odDocType
            );

            if (inserted > 0) {
                // 4.3.1 При успешном создании выгрузки сервис записывает ее параметры в кэш и использует для создания бандла
                log.info("Новая выгрузка для запроса с ID={} успешно записана в БД", unloadingRequestId);
                unloading = unloadingCrudService.findByUnloadingRequestId(unloadingRequestId);
                unloadingCache.put(unloadingRequestId, unloading);
            } else {
                // 4.3.2. При неуспешном создании выгрузки (unique constraint violation-проверка в БД на существующую выгрузку)
                // сервис делает запрос к таблице unloading в БД по unloaing_request_id,
                // записывает параметры выгрузки в кэш и использует их для нового бандла
                log.info("Не удалось записать выгрузку для запроса с ID={} в БД, делаем запрос к БД", unloadingRequestId);
                unloading = unloadingCrudService.findByUnloadingRequestId(unloadingRequestId);

                if (unloading != null) {
                    unloadingCache.put(unloadingRequestId, unloading);
                    log.info("Выгрузка для запроса с ID={} найдена в БД. Используем её для текущего запроса", unloadingRequestId);
                } else {
                    log.error("Выгрузка для запроса с ID={}, не найдена в БД после попытки записать новую.", unloadingRequestId);
                    return;
                }
            }
        }

        // 4.5 В случае, если статус выгрузки меньше нуля,
        // то сервис прекращает обработку бандла и возвращается слушать очередь системы источника
        if (unloading.getStateId() < 0) {
            log.error("У выгрузки для запроса с ID {} статус меньше 0, прекращаем обработку", unloadingRequestId);
            return;
        }

        // 5. Сервис создает запись в таблице bundle со статусом "Новый" (NEW_BUNDLE)
        BundleEntity bundleEntity = BundleEntity.builder()
                .status(BundleStatuses.NEW_BUNDLE.getStatus())
                .documentCount(unloading.getTotalDocs())
                .bundleNum(documentInputRequest.getBlockNum())
                .unloadingRequestId(unloading.getUnloadingRequestId())
                .type(OdDocTypes.getType(odDocType))
                .od_p(documentOperationalDayDate)
                .build();

        Long savedBundleId = bundleCrudService.save(bundleEntity).getId();

        // 6. В случае отсутствия ошибок сервис переходит к сохранению документов на диск и метаданных в БД (Use Case 03)
        log.info("Начало обработки бандла ID={} для выгрузки с ID={}", savedBundleId, unloadingRequestId);

        // 1. Сервис записывает информацию из бандла в БД:
        //  1.1 Метаданные в БД: таблица document
        List<DocumentEntity> savedDocuments = new ArrayList<>();
        try {
            for (Document documentFromRequest : allDocumentsFromRequest) {
                DocumentEntity entity = documentMapper.toDocumentEntity(
                        documentFromRequest,
                        documentTimeStamp,
                        savedBundleId,
                        unloadingRequestId,
                        documentOperationalDayDate
                );
                savedDocuments.add(entity);
            }

            documentCrudService.saveAll(savedDocuments);
            log.info("Сохранено {} документов в БД", savedDocuments.size());
        } catch (DbActionExecutionException e) {
            setBundleStatus(savedBundleId, BundleStatuses.SAVING_METADATA_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}", unloadingRequestId, e.getMessage(), e.getCause());
            return;
        }

        // 2. Сервис формирует ZIP файл c именем {id бандла}.zip, содержащий все декодированные из base64 файлы документов бандла.
        // 2.1 Имя файла каждого документа - {id документа}.{расширение}
        // 3. Сервис сохраняет в папке формата {YYYYMMDD}/{Тип документов} zip-файл со всеми документами бандла.
        try {
            bundleSaver.process(savedBundleId, savedDocuments, allDocumentsFromRequest, odDocType, documentOperationalDayDate);
        } catch (StorageException e) {
            // 4. В случае возникновения ошибок сохранения в CEPH:
            // - Сервис переводит бандл в статус "Ошибка сохранения на диске" (SAVING_FILER_ERROR) в БД
            // - Сервис возвращается к ожиданию сообщений по RESTу.
            setBundleStatus(savedBundleId, BundleStatuses.SAVING_FILER_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}", unloadingRequestId, e.getMessage(), e.getCause());
            return;
        }

        // После сохранения всей информации бандла сервис меняет значение статуса бандл на "Сохранен" (BUNDLE_SAVED)
        setBundleStatus(savedBundleId, BundleStatuses.BUNDLE_SAVED);
        log.info("Бандл для выгрузки с ID={} успешно сохранён", unloadingRequestId);
    }

    private void setBundleStatus(Long savedBundleId, BundleStatuses bundleStatus) {
        bundleCrudService.updateStatus(savedBundleId, bundleStatus.getStatus());
    }

    private LocalDateTime getOperationalDayDate(DocumentCard documentCard) {
        return documentCard.getVariableAttribute().stream()
                .filter(attr -> DocumentAttributeCodes.DOC_DATE.getCode().equals((attr.getAttributeCode())))
                .findFirst()
                .map(attr -> (LocalDateTime) attr.getAttributeValue())
                .orElse(null);
    }

    private Integer getDepartmentNumber(DocumentCard documentCard) {
        return documentCard.getVariableAttribute().stream()
                .filter(attr -> DocumentAttributeCodes.DOC_ACCOUNT.getCode().equals(attr.getAttributeCode()))
                .findFirst()
                .map(attr -> Integer.parseInt(attr.getAttributeValue().toString()))
                .orElse(null);
    }

    private String getSourceSystemCode(DocumentCard documentCard) {
        return documentCard.getVariableAttribute().stream()
                .filter(attr -> DocumentAttributeCodes.DOC_SOURCE_SYSTEM.getCode().equals((attr.getAttributeCode())))
                .findFirst()
                .map(attr -> attr.getAttributeValue().toString())
                .orElse(null);
    }
}