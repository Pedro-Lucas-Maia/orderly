package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreCapacityPolicy;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreServiceTest {

    private final StoreCapacityPolicy capacityPolicy = new FakeStoreCapacityPolicy();
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void pausesStoreWhenPendingOrdersReachItsLimit() {
        Store store = store();
        FakeStoreRepository stores = new FakeStoreRepository(store);
        StoreService service = new StoreService(stores, () -> 3, capacityPolicy, clock);

        service.refreshAutomaticStatuses(LocalTime.NOON);

        assertEquals(StoreStatus.PAUSADA, store.getStatus());
    }

    @Test
    void reopensStoreWhenPendingOrdersDropBelowItsLimit() {
        Store store = store();
        store.refreshAutomaticStatus(LocalTime.NOON, 3, capacityPolicy);
        FakeStoreRepository stores = new FakeStoreRepository(store);
        StoreService service = new StoreService(stores, () -> 2, capacityPolicy, clock);

        service.refreshAutomaticStatuses(LocalTime.NOON);

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void updatesManualStatusAndClearsIt() {
        Store store = store();
        FakeStoreRepository stores = new FakeStoreRepository(store);
        StoreService service = new StoreService(stores, () -> 0, capacityPolicy, clock);

        service.updateStatusManually(new UpdateStoreStatusInput(StoreStatus.FECHADA));
        assertEquals(StoreStatus.FECHADA, store.getStatus());

        service.clearManualStatus();
        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    private Store store() {
        return new Store(
                1L,
                "Dinner",
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                3,
                true,
                StoreStatus.FECHADA,
                "Rua",
                "123",
                "Centro",
                "Natal",
                "59000-000"
        );
    }

    private static class FakeStoreRepository implements StoreRepository {
        private Store store;

        FakeStoreRepository(Store store) {
            this.store = store;
        }

        @Override
        public Store save(Store store) {
            this.store = store;
            return store;
        }

        @Override
        public Optional<Store> getDefaultStore() {
            return Optional.ofNullable(store);
        }
    }

    private static class FakeStoreCapacityPolicy implements StoreCapacityPolicy {
        @Override
        public boolean canAcceptOrder(Store store, int currentOrdersInProgress) {
            return !store.isAutomaticPause() || currentOrdersInProgress < store.getMaxOrdersInProgress();
        }
    }
}
