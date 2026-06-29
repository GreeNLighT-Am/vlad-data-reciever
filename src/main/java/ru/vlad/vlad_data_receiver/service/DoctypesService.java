package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.DoctypesEntity;
import ru.vlad.vlad_data_receiver.repository.DoctypesRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctypesService {
    private final DoctypesRepository docTypesRepository;
    private final Map<String, DoctypesEntity> doctypesCache = new HashMap<>();

    @PostConstruct
    public void init() {
        fillCache();
    }

    private void fillCache() {
        docTypesRepository
                .findAll()
                .forEach(doctypesEntity ->
                        doctypesCache.put(doctypesEntity.getCode(), doctypesEntity));
    }

    private void evictCache() {
        doctypesCache.clear();
    }

    private void refreshCache() {
        evictCache();
        fillCache();
    }

    public boolean isDoctypesValid(String code) {
        if (doctypesCache.containsKey(code)) {
            return true;
        }

        refreshCache();

        return doctypesCache.containsKey(code);
    }
}