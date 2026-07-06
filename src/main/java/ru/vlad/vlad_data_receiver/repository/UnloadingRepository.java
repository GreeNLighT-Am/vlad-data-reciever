package ru.vlad.vlad_data_receiver.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.vlad.vlad_data_receiver.entity.UnloadingEntity;

@Repository
public interface UnloadingRepository extends ListCrudRepository<UnloadingEntity, Long> {
    UnloadingEntity findByUnloadingRequestId(String unloadingRequestId);
}