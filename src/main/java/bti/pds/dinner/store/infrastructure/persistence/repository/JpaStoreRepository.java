package bti.pds.dinner.store.infrastructure.persistence.repository;

import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.infrastructure.persistence.entity.StoreEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaStoreRepository implements StoreRepository {

    private final StoreEntityRepository repository;

    public JpaStoreRepository(StoreEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Store save(Store store) {
        StoreEntity savedEntity = repository.save(StoreEntity.from(store));
        return StoreEntity.toDomain(savedEntity);
    }

    @Override
    public Optional<Store> findById(Long id) {
        return repository.findById(id)
                .map(StoreEntity::toDomain);
    }
}
