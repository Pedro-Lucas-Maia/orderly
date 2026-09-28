package bti.pds.dinner.store.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.StoreAvailability;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public class JpaStoreAvailability implements StoreAvailability {

    private final StoreRepository storeRepository;
    private final OrdersInProgressCounter ordersInProgressCounter;

    public JpaStoreAvailability(
            StoreRepository storeRepository,
            OrdersInProgressCounter ordersInProgressCounter
    ) {
        this.storeRepository = storeRepository;
        this.ordersInProgressCounter = ordersInProgressCounter;
    }

    @Override
    public boolean isAvailableForOrders(Long storeId) {
        return storeRepository.findById(storeId)
                .map(this::refreshAndCheckAvailability)
                .orElse(false);
    }

    private boolean refreshAndCheckAvailability(Store store) {
        if (store.refreshAutomaticStatus(
                LocalTime.now(),
                ordersInProgressCounter.countOrdersInProgress(store.getId())
        )) {
            storeRepository.save(store);
        }
        return store.getStatus() == StoreStatus.ABERTA;
    }
}
