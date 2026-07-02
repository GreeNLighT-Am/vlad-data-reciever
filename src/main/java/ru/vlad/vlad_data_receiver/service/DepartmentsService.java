package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.DepartmentsEntity;
import ru.vlad.vlad_data_receiver.repository.DepartmentsRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentsService {
    private final DepartmentsRepository departmentsRepository;
    private Map<Integer, DepartmentsEntity> departmentsCache = new HashMap<>();

    @PostConstruct
    protected void init() {
        getDepartments();
    }

    private void getDepartments() {
        departmentsCache = departmentsRepository
                .findAll()
                .stream()
                .filter(DepartmentsEntity::getIsActive)
                .collect(Collectors.toMap(DepartmentsEntity::getCode, Function.identity()));
    }

    public boolean isDepartmentValid(Integer code) {
        if (departmentsCache.containsKey(code)) {
            return true;
        }
        getDepartments();
        return departmentsCache.containsKey(code);
    }
}
