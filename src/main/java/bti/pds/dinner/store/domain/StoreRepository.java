package bti.pds.dinner.store.domain;

import java.util.Optional;
public interface StoreRepository {
    Store save(Store store);
    Optional<Store> findById(Long id);
}
