package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.DepartmentsEntity;
import ru.vlad.vlad_data_receiver.repository.DepartmentsRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentsService {
    private final DepartmentsRepository departmentsRepository;
    private final Map<Integer, DepartmentsEntity> departmentsCache = new HashMap<>();

    @PostConstruct
    public void init() {
        fillCache();
    }

    private void fillCache() {
        departmentsRepository
                .findAll()
                .forEach(departmentsEntity ->
                        departmentsCache.put(departmentsEntity.getCode(), departmentsEntity));
    }

    private void evictCache() {
        departmentsCache.clear();
    }

    private void refreshCache() {
        evictCache();
        fillCache();
    }

    public boolean isDepartmentValid(Integer code) {
        if (departmentsCache.containsKey(code)) {
            return true;
        }

        refreshCache();

        return departmentsCache.containsKey(code);
    }
}
