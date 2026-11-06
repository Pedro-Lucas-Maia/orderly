package bti.pds.dinner.insights.domain;

import java.util.Optional;

public interface StoreCatalog {
    Optional<StoreRef> getDefaultStore();

    record StoreRef(Long id, String name) {
    }
}
