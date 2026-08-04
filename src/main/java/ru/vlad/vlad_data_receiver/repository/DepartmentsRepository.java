package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.repository.entity.DepartmentsEntity;

@Repository
public interface DepartmentsRepository extends ListCrudRepository<DepartmentsEntity, Integer> {
}
