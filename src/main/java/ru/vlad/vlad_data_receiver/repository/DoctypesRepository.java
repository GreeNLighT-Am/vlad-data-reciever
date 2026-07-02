package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.DoctypesEntity;

@Repository
public interface DoctypesRepository extends ListCrudRepository<DoctypesEntity, String> {
}
