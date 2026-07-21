package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.repository.entity.BundleEntity;

@Repository
public interface BundleRepository extends CrudRepository<BundleEntity, Long> {
    @Modifying
    @Query("UPDATE bundle SET status = :status WHERE id = :id")
    void updateStatus(Long id, String status);

    @Query("""
            SELECT COALESCE(SUM(document_count), 0) FROM bundle
            WHERE unloading_request_id = :unloadingRequestId AND status = 'BUNDLE_SAVED'
            """)
    int getTotalCompletedDocumentsCountByUnloadingRequestId(String unloadingRequestId);
}