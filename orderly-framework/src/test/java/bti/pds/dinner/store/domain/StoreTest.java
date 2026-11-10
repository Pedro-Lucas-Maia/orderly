package bti.pds.dinner.store.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreTest {

    private final StoreCapacityPolicy capacityPolicy = (store, currentOrders) ->
            !store.isAutomaticPause() || currentOrders < store.getMaxOrdersInProgress();

    @Test
    void throwsException_whenCapacityPolicyIsNull() {
        Store store = store(true);

        assertThrows(NullPointerException.class, () -> store.refreshAutomaticStatus(LocalTime.NOON, 0, null));
    }

    @Test
    void pausesAutomatically_whenDemandReachesConfiguredLimit() {
        Store store = store(true);

        assertTrue(store.refreshAutomaticStatus(LocalTime.NOON, 3, capacityPolicy));

        assertEquals(StoreStatus.PAUSADA, store.getStatus());
    }

    @Test
    void reopensAutomatically_whenDemandDropsBelowConfiguredLimit() {
        Store store = store(true);
        store.refreshAutomaticStatus(LocalTime.NOON, 3, capacityPolicy);

        assertTrue(store.refreshAutomaticStatus(LocalTime.NOON, 2, capacityPolicy));

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void doesNotPauseForDemand_whenAutomaticPauseIsDisabled() {
        Store store = store(false);

        store.refreshAutomaticStatus(LocalTime.NOON, 3, capacityPolicy);

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void preservesManualStatus_overAutomaticDemandRule() {
        Store store = store(true);
        store.setManualStatus(StoreStatus.ABERTA);

        assertFalse(store.refreshAutomaticStatus(LocalTime.NOON, 3, capacityPolicy));

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void closingTimeHasPriorityOverDemandRule() {
        Store store = store(true);

        store.refreshAutomaticStatus(LocalTime.of(22, 0), 3, capacityPolicy);

        assertEquals(StoreStatus.FECHADA, store.getStatus());
    }

    private Store store(boolean automaticPause) {
        return new Store(
                1L,
                "Dinner",
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                3,
                automaticPause,
                StoreStatus.FECHADA,
                "Rua",
                "123",
                "Centro",
                "Natal",
                "59000-000"
        );
    }
}
