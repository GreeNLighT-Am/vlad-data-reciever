package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.OperationalDay;

import java.time.LocalDate;

@Repository
public interface OperationalDayRepository extends CrudRepository<OperationalDay, Long> {

    int countByDateBetween(LocalDate start, LocalDate end);

    void deleteByDateBetween(LocalDate start, LocalDate end);

}