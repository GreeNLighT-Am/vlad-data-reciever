package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UnloadingRepository extends ListCrudRepository<UnloadingEntity, Integer> {
    Optional<UnloadingEntity> findByUnloadingRequestId(String unloadingRequestId);

    @Modifying
    @Query("""
            INSERT IGNORE INTO unloading
                (unloading_request_id, source_system_code, date, total_docs,
                 state_id, operational_day_id, department_number, doc_category)
            VALUES
                (:unloadingRequestId, :sourceSystemCode, :date, :totalDocs,
                 :stateId, :operationalDayId, :departmentNumber, :docCategory)
            """)
    void insertIfNotExists(String unloadingRequestId,
                           String sourceSystemCode,
                           LocalDateTime date,
                           Integer totalDocs,
                           int stateId,
                           Long operationalDayId,
                           Integer departmentNumber,
                           String docCategory);

    @Modifying
    @Query("INSERT IGNORE INTO unloading (unloading_request_id, state_id) VALUES (:unloadingRequestId, :stateId)")
    int setUnloadingStateId(String unloadingRequestId, int stateId);

    @Modifying
    @Query("UPDATE unloading SET state_id = :stateId WHERE unloading_request_id = :unloadingRequestId")
    void updateUnloadingStateId(String unloadingRequestId, int stateId);
}