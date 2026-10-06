package bti.pds.dinner.store.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreTest {

    @Test
    void pausesAutomatically_whenDemandReachesConfiguredLimit() {
        Store store = store(true);

        assertTrue(store.refreshAutomaticStatus(LocalTime.NOON, 3));

        assertEquals(StoreStatus.PAUSADA, store.getStatus());
    }

    @Test
    void reopensAutomatically_whenDemandDropsBelowConfiguredLimit() {
        Store store = store(true);
        store.refreshAutomaticStatus(LocalTime.NOON, 3);

        assertTrue(store.refreshAutomaticStatus(LocalTime.NOON, 2));

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void doesNotPauseForDemand_whenAutomaticPauseIsDisabled() {
        Store store = store(false);

        store.refreshAutomaticStatus(LocalTime.NOON, 3);

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void preservesManualStatus_overAutomaticDemandRule() {
        Store store = store(true);
        store.setManualStatus(StoreStatus.ABERTA);

        assertFalse(store.refreshAutomaticStatus(LocalTime.NOON, 3));

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void closingTimeHasPriorityOverDemandRule() {
        Store store = store(true);

        store.refreshAutomaticStatus(LocalTime.of(22, 0), 3);

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
                "1",
                "Centro",
                "Natal",
                "59000-000"
        );
    }
}
