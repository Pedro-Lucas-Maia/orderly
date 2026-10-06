package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.insights.domain.StoreCatalog;
import bti.pds.dinner.store.domain.StoreRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StoreCatalogAdapter implements StoreCatalog {
    private final StoreRepository storeRepository;

    public StoreCatalogAdapter(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public Optional<StoreRef> findById(Long storeId) {
        return storeRepository.findById(storeId)
                .map(store -> new StoreRef(store.getId(), store.getName()));
    }
}
