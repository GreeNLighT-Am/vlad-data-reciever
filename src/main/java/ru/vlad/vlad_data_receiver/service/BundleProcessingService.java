package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.exceptions.BundleProcessingException;
import ru.vlad.vlad_data_receiver.exceptions.FileSavingException;
import ru.vlad.vlad_data_receiver.mapper.DocumentMapper;
import ru.vlad.vlad_data_receiver.mapper.UnloadingMapper;
import ru.vlad.vlad_data_receiver.model.constants.BundleState;
import ru.vlad.vlad_data_receiver.model.constants.OdDocType;
import ru.vlad.vlad_data_receiver.model.constants.OperationalDayState;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingState;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.repository.entity.BundleEntity;
import ru.vlad.vlad_data_receiver.repository.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.repository.entity.OperationalDayEntity;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;
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
    private final UnloadingMapper unloadingMapper;

    @Transactional
    public void processBundle(DocumentInputRequest documentInputRequest) {
        String unloadingRequestId = documentInputRequest.getID();
        List<Document> allDocumentsFromRequest = documentInputRequest.getDocument();
        DocumentCard firstDocCard = allDocumentsFromRequest.get(0).getDocumentCard();
        LocalDate operationalDayDate = DocumentAttributeExtractor.getOperationalDayDate(firstDocCard);

        OperationalDayEntity operationalDay = operationalDayCrudService.findByDate(operationalDayDate);
        if (operationalDay.getStateId() != OperationalDayState.UNLOADING_RECEIVE_AVAILABLE.getStateId()) {
            processBundleProcessingError(
                    "Для запроса с ID=%s операционный день закрыт для приёма новых выгрузок".formatted(unloadingRequestId)
            );
        }

        LocalDateTime documentTimeStamp = documentInputRequest.getTimeStamp();
        String odDocType = documentInputRequest.getOdDocType();
        int totalDocs = documentInputRequest.getTotalDocs();

        UnloadingEntity unloading = unloadingMapper.toUnloadingEntity(
                unloadingRequestId,
                documentTimeStamp,
                totalDocs,
                operationalDay.getId(),
                odDocType,
                firstDocCard
        );

        unloading = processUnloading(unloading);

        Long savedBundleId = createNewBundle(
                documentInputRequest,
                allDocumentsFromRequest.size(),
                unloadingRequestId,
                odDocType,
                operationalDayDate
        );

        log.debug("Начало обработки бандла с ID={} для выгрузки с ID={}", savedBundleId, unloadingRequestId);
        try {
            int documentsSaved = saveDocuments(
                    allDocumentsFromRequest,
                    documentTimeStamp,
                    savedBundleId,
                    unloadingRequestId,
                    operationalDayDate,
                    odDocType
            );

            bundleCrudService.updateStatusById(savedBundleId, BundleState.BUNDLE_SAVED);
            log.info("Бандл с ID={} для выгрузки с ID={} успешно сохранён", savedBundleId, unloadingRequestId);

            setUnloadingStatus(unloading, documentsSaved, totalDocs);
        } catch (DbActionExecutionException e) {
            handleBundleSavingError(savedBundleId, unloadingRequestId, e, BundleState.SAVING_METADATA_ERROR);
        } catch (FileSavingException e) {
            handleBundleSavingError(savedBundleId, unloadingRequestId, e, BundleState.SAVING_FILE_ERROR);
        }
    }

    private UnloadingEntity processUnloading(UnloadingEntity newUnloading) {
        String unloadingRequestId = newUnloading.getUnloadingRequestId();
        UnloadingEntity unloading = unloadingCrudService.getOrCreateUnloading(newUnloading);

        int unloadingStateId = unloading.getStateId();
        if (unloadingStateId < 0) {
            processBundleProcessingError(
                    "У выгрузки для запроса с ID=%s статус меньше 0".formatted(unloadingRequestId)
            );
        } else if (unloadingStateId == UnloadingState.UNLOADING_SAVED.getStateId()) {
            processBundleProcessingError(
                    "Выгрузка с ID=%s уже была успешно сохранена ранее".formatted(unloadingRequestId)
            );
        }
        return unloading;
    }

    private Long createNewBundle(
            DocumentInputRequest documentInputRequest,
            int documentCount,
            String unloadingRequestId,
            String odDocType,
            LocalDate documentOperationalDayDate
    ) {
        BundleEntity bundleEntity = BundleEntity.builder()
                .status(BundleState.NEW_BUNDLE)
                .documentCount(documentCount)
                .bundleNum(documentInputRequest.getBlockNum())
                .unloadingRequestId(unloadingRequestId)
                .type(OdDocType.getType(odDocType))
                .od_p(documentOperationalDayDate)
                .build();

        return bundleCrudService.save(bundleEntity).getId();
    }

    private int saveDocuments(
            List<Document> allDocumentsFromRequest,
            LocalDateTime documentTimeStamp,
            Long savedBundleId,
            String unloadingRequestId,
            LocalDate operationalDayDate,
            String odDocType
    ) {
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

    private void setUnloadingStatus(UnloadingEntity unloading, int documentsSaved, int totalDocs) {
        String unloadingRequestId = unloading.getUnloadingRequestId();
        int docSavedInDb = bundleCrudService.getCompletedUnloadingsDocumentsCount(unloadingRequestId);

        if (documentsSaved == totalDocs || docSavedInDb == totalDocs) {
            unloadingCrudService.updateUnloadingStateId(unloading, UnloadingState.UNLOADING_SAVED);
            log.info("Получены все документы для выгрузки с ID={}", unloadingRequestId);
        } else if (documentsSaved > totalDocs || docSavedInDb > totalDocs) {
            unloadingCrudService.updateUnloadingStateId(unloading, UnloadingState.UNLOADING_ERROR);
            processBundleProcessingError(
                    "Для выгрузки с ID=%s передано больше документов чем ожидается".formatted(unloadingRequestId)
            );
        } else {
            log.info("Получена часть документов для выгрузки с ID={}", unloadingRequestId);
        }
    }

    private void handleBundleSavingError(Long bundleId, String unloadingRequestId, Exception e, BundleState errorState) {
        bundleCrudService.updateStatusById(bundleId, errorState);
        processBundleProcessingError(
                "Ошибка сохранения бандла для выгрузки с ID=%s: %s".formatted(unloadingRequestId, e.getMessage())
        );
    }

    private void processBundleProcessingError(String errorMessage) {
        log.error(errorMessage);
        throw new BundleProcessingException("Ошибка обработки бандла");
    }
}