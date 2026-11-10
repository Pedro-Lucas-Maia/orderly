package bti.pds.dinner.store.domain;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BurgerCapacityPolicy implements StoreCapacityPolicy {

    @Override
    public boolean canAcceptOrder(@NonNull Store store, int currentOrdersInProgress) {
        Objects.requireNonNull(store, "Store is required");
        if (currentOrdersInProgress < 0) {
            throw new IllegalArgumentException("Current orders in progress cannot be negative");
        }
        if (store.isAutomaticPause() && currentOrdersInProgress >= store.getMaxOrdersInProgress()) {
            return false;
        }
        return true;
    }
}
