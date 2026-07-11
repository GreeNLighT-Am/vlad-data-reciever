package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;
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
                                                Long stateId,
                                                Long operationalDayId,
                                                Integer departmentNumber,
                                                String docCategory) {
        log.info("Выгрузка для запроса с ID={} не найдена в кэше, создание новой", unloadingRequestId);

        int inserted = unloadingRepository.insertIfNotExists(unloadingRequestId, sourceSystemCode, date, totalDocs, stateId, operationalDayId, departmentNumber, docCategory);

        if (inserted > 0) {
            log.info("Новая выгрузка для запроса с ID={} успешно записана в БД", unloadingRequestId);
            return unloadingRepository.findByUnloadingRequestId(unloadingRequestId);
        } else {
            log.info("Не удалось записать выгрузку для запроса с ID={} в БД, поиск в БД", unloadingRequestId);
            UnloadingEntity unloading = unloadingRepository.findByUnloadingRequestId(unloadingRequestId);

            if (unloading != null) {
                log.info("Выгрузка для запроса с ID={} найдена в БД", unloadingRequestId);
                return unloading;
            } else {
                return null;
            }
        }
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