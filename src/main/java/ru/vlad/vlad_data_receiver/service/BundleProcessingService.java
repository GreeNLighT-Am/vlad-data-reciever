package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.exceptions.BundleProcessingException;
import ru.vlad.vlad_data_receiver.repository.entity.BundleEntity;
import ru.vlad.vlad_data_receiver.repository.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.repository.entity.OperationalDayEntity;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.exceptions.FileSavingException;
import ru.vlad.vlad_data_receiver.mapper.DocumentMapper;
import ru.vlad.vlad_data_receiver.model.constants.OdDocTypes;
import ru.vlad.vlad_data_receiver.model.constants.OperationalDayStates;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingStates;
import ru.vlad.vlad_data_receiver.model.constants.BundleStates;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.util.DocumentAttributeExtractor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleProcessingService {
    private final ArchiveProcessingService archiveProcessingService;
    private final DocumentCrudService documentCrudService;
    private final BundleCrudService bundleCrudService;
    private final UnloadingCrudService unloadingCrudService;
    private final OperationalDayCrudService operationalDayCrudService;
    private final DocumentMapper documentMapper;
    private static final String ERROR_MESSAGE = "Ошибка обработки бандла";

    @Transactional
    public void processBundle(DocumentInputRequest documentInputRequest) {
        String unloadingRequestId = documentInputRequest.getID();
        List<Document> allDocumentsFromRequest = documentInputRequest.getDocument();
        DocumentCard firstDocCard = allDocumentsFromRequest.get(0).getDocumentCard();
        LocalDate operationalDayDate = DocumentAttributeExtractor.getOperationalDayDate(firstDocCard);

        OperationalDayEntity operationalDay = operationalDayCrudService.findByDate(operationalDayDate, unloadingRequestId);
        if (operationalDay.getStateId() != OperationalDayStates.UNLOADING_RECEIVE_AVAILABLE.getStateId()) {
            processBundleProcessingError(String.format("Для запроса с ID=%s операционный день закрыт для приёма новых выгрузок, прекращаем обработку бандла", unloadingRequestId));
        }

        LocalDateTime documentTimeStamp = documentInputRequest.getTimeStamp();
        String odDocType = documentInputRequest.getOdDocType();
        int totalDocs = documentInputRequest.getTotalDocs();

        processUnloading(unloadingRequestId, firstDocCard, documentTimeStamp, totalDocs, operationalDay.getId(), odDocType);

        Long savedBundleId = createNewBundle(documentInputRequest, allDocumentsFromRequest.size(), unloadingRequestId, odDocType, operationalDayDate);

        log.debug("Начало обработки бандла с ID={} для выгрузки с ID={}", savedBundleId, unloadingRequestId);
        try {
            int documentsSaved = saveDocuments(allDocumentsFromRequest, documentTimeStamp, savedBundleId, unloadingRequestId, operationalDayDate, odDocType);

            bundleCrudService.updateStatus(savedBundleId, BundleStates.BUNDLE_SAVED.getStatus());
            log.info("Бандл с ID={} для выгрузки с ID={} успешно сохранён. Статус бандла переведён в BUNDLE_SAVED", savedBundleId, unloadingRequestId);

            setUnloadingStatus(unloadingRequestId, documentsSaved, totalDocs);
        } catch (DbActionExecutionException e) {
            handleBundleSavingError(savedBundleId, unloadingRequestId, e, BundleStates.SAVING_METADATA_ERROR);
        } catch (FileSavingException e) {
            handleBundleSavingError(savedBundleId, unloadingRequestId, e, BundleStates.SAVING_FILE_ERROR);
        }
    }

    private int saveDocuments(List<Document> allDocumentsFromRequest, LocalDateTime documentTimeStamp, Long savedBundleId, String unloadingRequestId, LocalDate operationalDayDate, String odDocType) {
        List<DocumentEntity> savedDocuments = allDocumentsFromRequest.stream()
                .map(doc -> documentMapper.toDocumentEntity(
                        doc,
                        documentTimeStamp,
                        savedBundleId,
                        unloadingRequestId,
                        operationalDayDate
                ))
                .collect(Collectors.toList());
        documentCrudService.saveAll(savedDocuments);

        int documentsSaved = savedDocuments.size();
        log.debug("Сохранено {} документов в БД", documentsSaved);

        archiveProcessingService.process(savedBundleId, savedDocuments, allDocumentsFromRequest, odDocType, operationalDayDate);
        return documentsSaved;
    }

    private void processBundleProcessingError(String errorMessage) {
        log.error(errorMessage);
        throw new BundleProcessingException(ERROR_MESSAGE);
    }

    private void handleBundleSavingError(Long bundleId, String unloadingRequestId, Exception e, BundleStates errorState) {
        bundleCrudService.updateStatus(bundleId, errorState.getStatus());
        processBundleProcessingError(String.format(
                "Ошибка сохранения бандла для выгрузки с ID=%s: %s. Статус бандла переведён в %s",
                unloadingRequestId, e.getMessage(), errorState.name()
        ));
    }

    private Long createNewBundle(DocumentInputRequest documentInputRequest, int documentCount, String unloadingRequestId, String odDocType, LocalDate documentOperationalDayDate) {
        BundleEntity bundleEntity = BundleEntity.builder()
                .status(BundleStates.NEW_BUNDLE.getStatus())
                .documentCount(documentCount)
                .bundleNum(documentInputRequest.getBlockNum())
                .unloadingRequestId(unloadingRequestId)
                .type(OdDocTypes.getType(odDocType))
                .od_p(documentOperationalDayDate)
                .build();

        return bundleCrudService.save(bundleEntity).getId();
    }

    private void processUnloading(String unloadingRequestId, DocumentCard documentCardOfFirstDocumentFromRequest, LocalDateTime documentTimeStamp, int totalDocs, Long operationalDayId, String odDocType) {
        UnloadingEntity unloading = unloadingCrudService.getOrCreateUnloading(
                unloadingRequestId,
                DocumentAttributeExtractor.getSourceSystemCode(documentCardOfFirstDocumentFromRequest),
                documentTimeStamp,
                totalDocs,
                UnloadingStates.NEW_UNLOADING.getStateId(),
                operationalDayId,
                DocumentAttributeExtractor.getDepartmentNumber(documentCardOfFirstDocumentFromRequest),
                odDocType
        );

        int unloadingStateId = unloading.getStateId();
        if (unloadingStateId < 0) {
            processBundleProcessingError(String.format("У выгрузки для запроса с ID=%s статус меньше 0, прекращаем обработку бандла", unloadingRequestId));
        } else if (unloadingStateId == UnloadingStates.UNLOADING_SAVED.getStateId()) {
            processBundleProcessingError(String.format("Выгрузка с ID=%s уже была успешно сохранена ранее, прекращаем обработку бандла", unloadingRequestId));
        }
    }

    private void setUnloadingStatus(String unloadingRequestId, int documentsSaved, int totalDocs) {
        int docSavedInDb = bundleCrudService.getTotalCompletedDocumentsCountByUnloadingRequestId(unloadingRequestId);
        if (documentsSaved == totalDocs || docSavedInDb == totalDocs) {
            unloadingCrudService.updateUnloadingStateId(unloadingRequestId, UnloadingStates.UNLOADING_SAVED);
            log.info("Получены все документы для выгрузки с ID={}. Статус выгрузки переведён в UNLOADING_SAVED", unloadingRequestId);
        } else if (documentsSaved > totalDocs || docSavedInDb > totalDocs) {
            unloadingCrudService.updateUnloadingStateId(unloadingRequestId, UnloadingStates.UNLOADING_ERROR);
            processBundleProcessingError(String.format("Для выгрузки с ID=%s передано больше документов чем ожидается. Статус выгрузки переведён в UNLOADING_ERROR", unloadingRequestId));
        } else {
            log.info("Получена часть документов для выгрузки с ID={}", unloadingRequestId);
        }
    }
}