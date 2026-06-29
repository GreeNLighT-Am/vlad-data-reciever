package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.SourceSystemsEntity;
import ru.vlad.vlad_data_receiver.repository.SourceSystemsRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SourceSystemsService {
    private final SourceSystemsRepository sourceSystemsRepository;
    private final Map<String, SourceSystemsEntity> sourceSystemsCache = new HashMap<>();

    @PostConstruct
    public void init() {
        fillCache();
    }

    private void fillCache() {
        sourceSystemsRepository
                .findAll()
                .forEach(sourceSystemsEntity ->
                        sourceSystemsCache.put(sourceSystemsEntity.getCode(), sourceSystemsEntity));
    }

    private void evictCache() {
        sourceSystemsCache.clear();
    }

    private void refreshCache() {
        evictCache();
        fillCache();
    }

    public boolean isSourceSystemValid(String code) {
        if (sourceSystemsCache.containsKey(code)) {
            return true;
        }

        refreshCache();

        return sourceSystemsCache.containsKey(code);
    }
}