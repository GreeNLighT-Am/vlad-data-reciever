package ru.vlad.vlad_data_receiver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.repository.entity.SourceSystemsEntity;
import ru.vlad.vlad_data_receiver.repository.SourceSystemsRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceSystemsService {
    private final SourceSystemsRepository sourceSystemsRepository;
    private Map<String, SourceSystemsEntity> sourceSystemsCache = new HashMap<>();

    @PostConstruct
    protected void init() {
        getSourceSystems();
    }

    private void getSourceSystems() {
        sourceSystemsCache = sourceSystemsRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(SourceSystemsEntity::getCode, Function.identity()));
    }

    public boolean isSourceSystemValid(String code) {
        if (sourceSystemsCache.containsKey(code)) {
            return true;
        }
        getSourceSystems();
        return sourceSystemsCache.containsKey(code);
    }
}