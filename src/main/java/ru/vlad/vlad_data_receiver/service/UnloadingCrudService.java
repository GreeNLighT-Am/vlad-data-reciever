package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.repository.UnloadingRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnloadingCrudService {
    private final UnloadingRepository unloadingRepository;


    @Transactional(readOnly = true)
    public UnloadingEntity findByUnloadingRequestId(String unloadingRequestId) {
        return unloadingRepository.findByUnloadingRequestId(unloadingRequestId);
    }

    @Transactional
    public int insertIfNotExists(String unloadingRequestId,
                                             String sourceSystemCode,
                                             LocalDateTime date,
                                             Integer totalDocs,
                                             Long stateId,
                                             Long operationalDayId,
                                             Integer departmentNumber,
                                             String docCategory) {
        return unloadingRepository.insertIfNotExists(unloadingRequestId, sourceSystemCode, date, totalDocs, stateId, operationalDayId, departmentNumber, docCategory);
    }
}