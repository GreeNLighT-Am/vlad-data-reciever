package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.repository.UnloadingRepository;

@Service
@RequiredArgsConstructor
public class UnloadingCrudService {
    private final UnloadingRepository unloadingRepository;

    @Transactional(readOnly = true)
    public UnloadingEntity findByUnloadingRequestId(String unloadingRequestId) {
        return unloadingRepository.findByUnloadingRequestId(unloadingRequestId);
    }

    @Transactional
    public UnloadingEntity save(UnloadingEntity unloadingEntity) {
        return unloadingRepository.save(unloadingEntity);
    }
}