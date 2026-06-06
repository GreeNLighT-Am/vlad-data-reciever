package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.SourceSystemsEntity;
import ru.vlad.vlad_data_receiver.repository.SourceSystemsRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SourceSystemsService {
    private final SourceSystemsRepository sourceSystemsRepository;
    private Map<String, SourceSystemsEntity> sourceSystemsCache = new HashMap<>();

    @PostConstruct
    @Transactional(readOnly = true)
    public void init() {
        sourceSystemsRepository
                .findAll()
                .forEach(sourceSystemsEntity ->
                        sourceSystemsCache.put(sourceSystemsEntity.getCode(), sourceSystemsEntity));
    }

    public boolean isSourceSystemValid(String code) {
        return sourceSystemsCache.containsKey(code);
    }
}