package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.repository.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.repository.DocumentRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCrudService {
    private final DocumentRepository documentRepository;

    @Transactional
    public void saveAll(List<DocumentEntity> documentEntity) {
        documentRepository.saveAll(documentEntity);
        log.debug("Выполнена пакетная вставка документов");
    }

    @Transactional(readOnly = true)
    public int countByUnloadingRequestId(String unloadingRequestId) {
        return documentRepository.countByUnloadingRequestId(unloadingRequestId);
    }
}