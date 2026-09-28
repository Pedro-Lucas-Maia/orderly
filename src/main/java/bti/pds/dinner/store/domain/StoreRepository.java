package bti.pds.dinner.store.domain;

import java.util.Optional;
import java.util.List;
public interface StoreRepository {
    Store save(Store store);
    Optional<Store> findById(Long id);
    List<Store> findAll();
}