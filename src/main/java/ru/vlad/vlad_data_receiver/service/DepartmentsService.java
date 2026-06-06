package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.DepartmentsEntity;
import ru.vlad.vlad_data_receiver.repository.DepartmentsRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentsService {
    private final DepartmentsRepository departmentsRepository;
    private Map<String, DepartmentsEntity> departmentsCache = new HashMap<>();

    @PostConstruct
    @Transactional(readOnly = true)
    public void init() {
        departmentsRepository
                .findAll()
                .forEach(departments ->
                        departmentsCache.put(departments.getCode(), departments));
    }

    public boolean isDepartmentValid(String code) {
        return departmentsCache.containsKey(code);
    }
}
