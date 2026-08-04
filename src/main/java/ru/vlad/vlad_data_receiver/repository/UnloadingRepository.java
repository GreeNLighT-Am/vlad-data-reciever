package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;

import java.util.Optional;

@Repository
public interface UnloadingRepository extends CrudRepository<UnloadingEntity, Long> {
    Optional<UnloadingEntity> findByUnloadingRequestId(String unloadingRequestId);

    @Modifying
    @Query("""
            INSERT IGNORE INTO unloading
                (unloading_request_id, source_system_code, date, total_docs,
                 state_id, operational_day_id, department_number, doc_category)
            VALUES
                (:#{#unloading.unloadingRequestId},
                 :#{#unloading.sourceSystemCode},
                 :#{#unloading.date},
                 :#{#unloading.totalDocs},
                 :#{#unloading.stateId},
                 :#{#unloading.operationalDayId},
                 :#{#unloading.departmentNumber},
                 :#{#unloading.docCategory})
            """)
    void insertIfNotExists(UnloadingEntity unloading);

    // Метод используется для создания ошибочной выгрузки
    @Modifying
    @Query("INSERT IGNORE INTO unloading (unloading_request_id, state_id) VALUES (:unloadingRequestId, :stateId)")
    int createErrorUnloading(String unloadingRequestId, int stateId);

    @Modifying
    @Query("UPDATE unloading SET state_id = :stateId WHERE unloading_request_id = :unloadingRequestId")
    void updateUnloadingStateId(String unloadingRequestId, int stateId);
}