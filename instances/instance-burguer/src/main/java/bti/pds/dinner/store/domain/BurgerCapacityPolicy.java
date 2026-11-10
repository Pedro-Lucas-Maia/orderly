package bti.pds.dinner.store.domain;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BurgerCapacityPolicy implements StoreCapacityPolicy {

    @Override
    public boolean canAcceptOrder(@NonNull Store store, int currentOrdersInProgress) {
        if (store.isAutomaticPause() && currentOrdersInProgress >= store.getMaxOrdersInProgress()) {
            return false;
        }
        return true;
    }
}
