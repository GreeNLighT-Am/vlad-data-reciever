package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, Long> {
}