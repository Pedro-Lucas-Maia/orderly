package bti.pds.dinner.store.infrastructure.persistence.repository;

import bti.pds.dinner.store.infrastructure.persistence.entity.StoreEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreEntityRepository extends CrudRepository<StoreEntity, Long> {
}
