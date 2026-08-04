package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.model.constants.BundleState;
import ru.vlad.vlad_data_receiver.repository.BundleRepository;
import ru.vlad.vlad_data_receiver.repository.entity.BundleEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCrudService {
    private final BundleRepository bundleRepository;

    @Transactional
    public BundleEntity save(BundleEntity bundleEntity) {
        return bundleRepository.save(bundleEntity);
    }

    @Transactional
    public void updateStatusById(Long id, BundleState status) {
        bundleRepository.updateBundleStatusById(id, status);
    }

    @Transactional
    public int getCompletedUnloadingsDocumentsCount(String unloadingRequestId) {
        return bundleRepository.getCompletedUnloadingsDocumentsCount(unloadingRequestId);
    }
}