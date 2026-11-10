package bti.pds.dinner.store.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.StoreAvailability;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreCapacityPolicy;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalTime;

@Repository
public class JpaStoreAvailability implements StoreAvailability {

    private final StoreRepository storeRepository;
    private final OrdersInProgressCounter ordersInProgressCounter;
    private final StoreCapacityPolicy storeCapacityPolicy;
    private final Clock clock;

    public JpaStoreAvailability(
            StoreRepository storeRepository,
            OrdersInProgressCounter ordersInProgressCounter,
            StoreCapacityPolicy storeCapacityPolicy,
            Clock clock
    ) {
        this.storeRepository = storeRepository;
        this.ordersInProgressCounter = ordersInProgressCounter;
        this.storeCapacityPolicy = storeCapacityPolicy;
        this.clock = clock;
    }

    @Override
    public boolean isAvailableForOrders() {
        return storeRepository.getDefaultStore()
                .map(this::refreshAndCheckAvailability)
                .orElse(false);
    }

    private boolean refreshAndCheckAvailability(Store store) {
        if (store.refreshAutomaticStatus(
                LocalTime.now(clock),
                ordersInProgressCounter.countOrdersInProgress(),
                storeCapacityPolicy
        )) {
            storeRepository.save(store);
        }
        return store.getStatus() == StoreStatus.ABERTA;
    }
}
