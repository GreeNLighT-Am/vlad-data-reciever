package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;

import java.time.LocalDateTime;

@Repository
public interface UnloadingRepository extends ListCrudRepository<UnloadingEntity, Long> {
    UnloadingEntity findByUnloadingRequestId(String unloadingRequestId);

    @Modifying
    @Query("""
        INSERT IGNORE INTO unloading 
            (unloading_request_id, source_system_code, date, total_docs, 
             state_id, operational_day_id, department_number, doc_category)
        VALUES 
            (:unloadingRequestId, :sourceSystemCode, :date, :totalDocs, 
             :stateId, :operationalDayId, :departmentNumber, :docCategory)
        """)
    int insertIfNotExists(@Param("unloadingRequestId") String unloadingRequestId,
                          @Param("sourceSystemCode") String sourceSystemCode,
                          @Param("date") LocalDateTime date,
                          @Param("totalDocs") Integer totalDocs,
                          @Param("stateId") Long stateId,
                          @Param("operationalDayId") Long operationalDayId,
                          @Param("departmentNumber") Integer departmentNumber,
                          @Param("docCategory") String docCategory);
}