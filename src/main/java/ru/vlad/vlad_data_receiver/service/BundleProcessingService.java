package ru.vlad.vlad_data_receiver.service;

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
import ru.vlad.vlad_data_receiver.model.constants.OperationalDayStates;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingStates;
import ru.vlad.vlad_data_receiver.saver.BundleSaver;
import ru.vlad.vlad_data_receiver.model.constants.BundleStates;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public void processBundle(DocumentInputRequest documentInputRequest) {
        String unloadingRequestId = documentInputRequest.getID();
        List<Document> allDocumentsFromRequest = documentInputRequest.getDocument();
        DocumentCard documentCardOfFirstDocumentFromRequest = allDocumentsFromRequest.get(0).getDocumentCard();
        LocalDate documentOperationalDayDate = getOperationalDayDate(documentCardOfFirstDocumentFromRequest).toLocalDate();

        OperationalDayEntity operationalDay = operationalDayCrudService.findByDate(documentOperationalDayDate);
        if (operationalDay == null) {
            log.error("Для выгрузки с ID={} не найден операционный день", unloadingRequestId);
            return;
        } else if (operationalDay.getStateId().equals(OperationalDayStates.UNLOADING_RECEIVE_STOPPED.getStateId())) {
            log.error("Для выгрузки с ID={} операционный день находится в статусе UNLOADING_RECEIVE_STOPPED", unloadingRequestId);
            return;
        }

        LocalDateTime documentTimeStamp = documentInputRequest.getTimeStamp();
        String odDocType = documentInputRequest.getOdDocType();
        int totalDocs = documentInputRequest.getTotalDocs();

        UnloadingEntity unloading = unloadingCrudService.getOrCreateUnloading(unloadingRequestId,
                getSourceSystemCode(documentCardOfFirstDocumentFromRequest),
                documentTimeStamp,
                totalDocs,
                UnloadingStates.NEW_UNLOADING.getStateId(),
                operationalDay.getId(),
                getDepartmentNumber(documentCardOfFirstDocumentFromRequest),
                odDocType);

        if (unloading == null) {
            log.error("Для запроса с ID={} не найдена в кеше, а также не удалось создать новую выгрузку или получить её из БД", unloadingRequestId);
            return;
        }

        if (unloading.getStateId() < 0) {
            log.error("У выгрузки для запроса с ID {} статус меньше 0, прекращаем обработку", unloadingRequestId);
            return;
        } else if (unloading.getStateId().equals(UnloadingStates.UNLOADING_SAVED.getStateId())) {
            log.error("Выгрузка с ID={} была успешно сохранена ранее", unloadingRequestId);
            return;
        }

        BundleEntity bundleEntity = BundleEntity.builder()
                .status(BundleStates.NEW_BUNDLE.getStatus())
                .documentCount(allDocumentsFromRequest.size())
                .bundleNum(documentInputRequest.getBlockNum())
                .unloadingRequestId(unloadingRequestId)
                .type(OdDocTypes.getType(odDocType))
                .od_p(documentOperationalDayDate)
                .build();

        Long savedBundleId = bundleCrudService.save(bundleEntity).getId();

        log.info("Начало обработки бандла ID={} для выгрузки с ID={}", savedBundleId, unloadingRequestId);

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
            setBundleStatus(savedBundleId, BundleStates.SAVING_METADATA_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}. Статус бандла переведён в SAVING_METADATA_ERROR", unloadingRequestId, e.getMessage(), e);
            return;
        }

        try {
            bundleSaver.process(savedBundleId, savedDocuments, allDocumentsFromRequest, odDocType, documentOperationalDayDate);
        } catch (StorageException e) {
            setBundleStatus(savedBundleId, BundleStates.SAVING_FILER_ERROR);
            log.error("Ошибка сохранения бандла для выгрузки с ID={}: {}. Статус бандла переведён в SAVING_FILER_ERROR", unloadingRequestId, e.getMessage(), e);
            return;
        }

        setBundleStatus(savedBundleId, BundleStates.BUNDLE_SAVED);
        log.info("Бандл для выгрузки с ID={} успешно сохранён. Статус бандла переведён в BUNDLE_SAVED", unloadingRequestId);

        if (savedDocuments.size() == totalDocs || documentCrudService.countByUnloadingRequestId(unloadingRequestId) == totalDocs) {
            unloadingCrudService.updateUnloadingStateId(unloadingRequestId, UnloadingStates.UNLOADING_SAVED);
            log.info("Получены все документы для выгрузки с ID={}. Статус выгрузки переведён в UNLOADING_SAVED", unloadingRequestId);
        } else {
            log.info("Получена часть документов для выгрузки с ID={}", unloadingRequestId);
        }
    }

    private void setBundleStatus(Long savedBundleId, BundleStates bundleStatus) {
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