package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.exceptions.UnloadingNotFoundException;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingStates;
import ru.vlad.vlad_data_receiver.repository.UnloadingRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnloadingCrudService {
    private final UnloadingRepository unloadingRepository;

    @Transactional
    @Cacheable(value = "unloadingCache", key = "#unloadingRequestId", unless = "#result == null")
    public UnloadingEntity getOrCreateUnloading(String unloadingRequestId,
                                                String sourceSystemCode,
                                                LocalDateTime date,
                                                Integer totalDocs,
                                                int stateId,
                                                Long operationalDayId,
                                                Integer departmentNumber,
                                                String docCategory) {
        log.debug("Для запроса с ID={} выгрузка не найдена в кэше, попытка записать новую или найти существующую в БД", unloadingRequestId);

        unloadingRepository.insertIfNotExists(unloadingRequestId, sourceSystemCode, date, totalDocs, stateId, operationalDayId, departmentNumber, docCategory);

        return unloadingRepository.findByUnloadingRequestId(unloadingRequestId).orElseThrow(() -> {
            String errorMessage = String.format("Для запроса с ID=%s выгрузка не найдена в кеше, а также не удалось создать новую выгрузку или получить существующую из БД", unloadingRequestId);
            log.error(errorMessage);
            return new UnloadingNotFoundException(errorMessage);
        });
    }

    @Transactional
    public int setUnloadingStateId(String unloadingRequestId, UnloadingStates unloadingStateId) {
        return unloadingRepository.setUnloadingStateId(unloadingRequestId, unloadingStateId.getStateId());
    }

    @Transactional
    public void updateUnloadingStateId(String unloadingRequestId, UnloadingStates unloadingStateId) {
        unloadingRepository.updateUnloadingStateId(unloadingRequestId, unloadingStateId.getStateId());
    }
}