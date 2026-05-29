package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.OperationalDayEntity;

import java.time.LocalDate;

@Repository
public interface OperationalDayRepository extends CrudRepository<OperationalDayEntity, Long> {

    int countByDateBetween(LocalDate start, LocalDate end);

    @Modifying
    int deleteByDateBetween(LocalDate start, LocalDate end);

}