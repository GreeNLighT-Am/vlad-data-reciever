package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.DoctypesEntity;
import ru.vlad.vlad_data_receiver.repository.DoctypesRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctypesService {
    private final DoctypesRepository docTypesRepository;
    private Map<String, DoctypesEntity> doctypesCache = new HashMap<>();

    @PostConstruct
    @Transactional(readOnly = true)
    public void init() {
        docTypesRepository
                .findAll()
                .forEach(doctypesEntity ->
                        doctypesCache.put(doctypesEntity.getCode(), doctypesEntity));
    }

    public boolean isDoctypesValid(String code) {
        return doctypesCache.containsKey(code);
    }
}