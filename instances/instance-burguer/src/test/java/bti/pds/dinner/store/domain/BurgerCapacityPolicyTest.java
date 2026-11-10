package bti.pds.dinner.store.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurgerCapacityPolicyTest {

    private final BurgerCapacityPolicy policy = new BurgerCapacityPolicy();

    @Test
    void throwsException_whenStoreIsNull() {
        assertThrows(NullPointerException.class, () -> policy.canAcceptOrder(null, 0));
    }

    @Test
    void throwsException_whenCurrentOrdersInProgressIsNegative() {
        Store store = store(true, 5);

        assertThrows(IllegalArgumentException.class, () -> policy.canAcceptOrder(store, -1));
    }

    @Test
    void acceptsOrder_whenOrdersInProgressBelowLimit() {
        Store store = store(true, 5);

        assertTrue(policy.canAcceptOrder(store, 4));
    }

    @Test
    void rejectsOrder_whenOrdersInProgressReachesLimitAndAutomaticPauseEnabled() {
        Store store = store(true, 5);

        assertFalse(policy.canAcceptOrder(store, 5));
    }

    @Test
    void rejectsOrder_whenOrdersInProgressExceedsLimitAndAutomaticPauseEnabled() {
        Store store = store(true, 5);

        assertFalse(policy.canAcceptOrder(store, 6));
    }

    @Test
    void acceptsOrder_whenAutomaticPauseDisabledEvenIfExceedingLimit() {
        Store store = store(false, 5);

        assertTrue(policy.canAcceptOrder(store, 10));
    }

    private Store store(boolean automaticPause, int maxOrdersInProgress) {
        return new Store(
                1L,
                "Burger Place",
                LocalTime.of(18, 0),
                LocalTime.of(23, 0),
                maxOrdersInProgress,
                automaticPause,
                StoreStatus.ABERTA,
                "Street",
                "10",
                "Downtown",
                "Natal",
                "59000-000"
        );
    }
}
