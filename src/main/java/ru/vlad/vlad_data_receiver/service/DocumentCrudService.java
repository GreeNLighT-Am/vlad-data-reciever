package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.repository.DocumentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCrudService {
    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentEntity save(DocumentEntity documentEntity) {
        return documentRepository.save(documentEntity);
    }
}