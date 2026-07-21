package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.repository.entity.BundleEntity;
import ru.vlad.vlad_data_receiver.repository.BundleRepository;

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
    public void updateStatus(Long id, String status) {
        bundleRepository.updateStatus(id, status);
    }

    @Transactional
    public int getTotalCompletedDocumentsCountByUnloadingRequestId(String unloadingRequestId) {
        return bundleRepository.getTotalCompletedDocumentsCountByUnloadingRequestId(unloadingRequestId);
    }
}