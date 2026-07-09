package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.BundleEntity;

@Repository
public interface BundleRepository extends CrudRepository<BundleEntity, Long> {
    @Modifying
    @Query("UPDATE bundle SET status = :status WHERE id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}