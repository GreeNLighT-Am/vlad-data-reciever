package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.repository.entity.DoctypesEntity;
import ru.vlad.vlad_data_receiver.repository.DoctypesRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctypesService {
    private final DoctypesRepository docTypesRepository;
    private Map<String, DoctypesEntity> doctypesCache = new HashMap<>();

    @PostConstruct
    protected void init() {
        getDoctypes();
    }

    private void getDoctypes() {
        doctypesCache = docTypesRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(DoctypesEntity::getCode, Function.identity()));
    }

    public boolean isDoctypesValid(String code) {
        if (doctypesCache.containsKey(code)) {
            return true;
        }
        getDoctypes();
        return doctypesCache.containsKey(code);
    }
}