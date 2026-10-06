package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreServiceTest {

    @Test
    void pausesStoreWhenPendingOrdersReachItsLimit() {
        Store store = store();
        FakeStoreRepository stores = new FakeStoreRepository(store);
        StoreService service = new StoreService(stores, storeId -> 3);

        service.refreshAutomaticStatuses(LocalTime.NOON);

        assertEquals(StoreStatus.PAUSADA, store.getStatus());
    }

    @Test
    void reopensStoreWhenPendingOrdersDropBelowItsLimit() {
        Store store = store();
        store.refreshAutomaticStatus(LocalTime.NOON, 3);
        FakeStoreRepository stores = new FakeStoreRepository(store);
        StoreService service = new StoreService(stores, storeId -> 2);

        service.refreshAutomaticStatuses(LocalTime.NOON);

        assertEquals(StoreStatus.ABERTA, store.getStatus());
    }

    @Test
    void appliesDemandOnlyToTheStoreThatOwnsThePendingOrders() {
        Store firstStore = store();
        Store secondStore = new Store(
                2L,
                "Dinner Centro",
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
        FakeStoreRepository stores = new FakeStoreRepository(firstStore, secondStore);
        StoreService service = new StoreService(stores, storeId -> storeId == 1L ? 3 : 0);

        service.refreshAutomaticStatuses(LocalTime.NOON);

        assertEquals(StoreStatus.PAUSADA, firstStore.getStatus());
        assertEquals(StoreStatus.ABERTA, secondStore.getStatus());
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
        private final List<Store> stores = new ArrayList<>();

        FakeStoreRepository(Store... stores) {
            this.stores.addAll(List.of(stores));
        }

        @Override
        public Store save(Store store) {
            return store;
        }

        @Override
        public Optional<Store> findById(Long id) {
            return stores.stream().filter(store -> store.getId().equals(id)).findFirst();
        }

        @Override
        public List<Store> findAll() {
            return stores;
        }
    }
}
