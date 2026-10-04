package bti.pds.dinner.insights.domain;

import java.util.List;
import java.util.Optional;

public interface StoreCatalog {
    Optional<StoreRef> findById(Long storeId);

    record StoreRef(Long id, String name) {
    }
}
