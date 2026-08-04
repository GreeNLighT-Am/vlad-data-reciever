package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.exceptions.UnloadingNotFoundException;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingState;
import ru.vlad.vlad_data_receiver.repository.UnloadingRepository;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnloadingCrudService {
    private final UnloadingRepository unloadingRepository;

    @Transactional
    @Cacheable(value = "unloadingCache", key = "#unloading.unloadingRequestId", unless = "#result == null")
    public UnloadingEntity getOrCreateUnloading(UnloadingEntity unloading) {
        String unloadingRequestId = unloading.getUnloadingRequestId();
        log.debug("Для запроса с ID={} выгрузка не найдена в кэше", unloadingRequestId);

        unloadingRepository.insertIfNotExists(unloading);

        return unloadingRepository.findByUnloadingRequestId(unloadingRequestId).orElseThrow(() -> {
            String errorMessage = "Для запроса с ID=%s не удалось получить выгрузку из БД".formatted(
                    unloadingRequestId
            );
            log.error(errorMessage);
            return new UnloadingNotFoundException(errorMessage);
        });
    }

    @Transactional
    public int setUnloadingStateId(String unloadingRequestId, UnloadingState unloadingStateId) {
        return unloadingRepository.createErrorUnloading(unloadingRequestId, unloadingStateId.getStateId());
    }

    @Transactional
    @CachePut(value = "unloadingCache", key = "#result.unloadingRequestId")
    public UnloadingEntity updateUnloadingStateId(UnloadingEntity unloading, UnloadingState unloadingStateId) {
        int stateId = unloadingStateId.getStateId();
        unloading.setStateId(stateId);
        unloadingRepository.updateUnloadingStateId(unloading.getUnloadingRequestId(), stateId);
        return unloading;
    }
}