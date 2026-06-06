package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.SourceSystemsEntity;

@Repository
public interface SourceSystemsRepository extends CrudRepository<SourceSystemsEntity, String> {
}
