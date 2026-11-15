package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.application.input.UpdateStoreSettingsInput;
import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreCapacityPolicy;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalTime;

@Slf4j
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final OrdersInProgressCounter ordersInProgressCounter;
    private final StoreCapacityPolicy storeCapacityPolicy;
    private final Clock clock;

    public StoreService(
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

    @Transactional(readOnly = true)
    public StoreOutput getStore() {
        return toOutput(findStore());
    }

    @Transactional
    @Observed(name = "store.admin.update-settings")
    public StoreOutput updateSettings(@NonNull UpdateStoreSettingsInput input) {
        Store store = findStore();
        store.updateSettings(
                input.openingTime(),
                input.closingTime(),
                input.maxOrdersInProgress(),
                input.automaticPause()
        );
        refreshAutomaticStatus(store, currentTime());
        var saved = storeRepository.save(store);
        log.info("Store settings updated. storeId={}", saved.getId());
        return toOutput(saved);
    }

    @Transactional
    @Observed(name = "store.admin.update-status-manually")
    public StoreOutput updateStatusManually(@NonNull UpdateStoreStatusInput input) {
        Store store = findStore();
        var oldStatus = store.getStatus();
        store.setManualStatus(input.status());
        var saved = storeRepository.save(store);
        log.info("store status updated manually. storeId={}, oldStatus={}, newStatus={}",
                saved.getId(), oldStatus, saved.getStatus());
        return toOutput(saved);
    }

    @Transactional
    @Observed(name = "store.admin.clear-manual-status")
    public StoreOutput clearManualStatus() {
        Store store = findStore();
        var oldStatus = store.getStatus();
        store.clearManualStatus();
        refreshAutomaticStatus(store, currentTime());
        var saved = storeRepository.save(store);
        log.info("store status clear manually. storeId={}, oldStatus={}, newStatus={}",
                saved.getId(), oldStatus, saved.getStatus());
        return toOutput(saved);
    }

    @Transactional
    public void refreshAutomaticStatuses(LocalTime currentTime) {
        Store store = findStore();
        var oldStatus = store.getStatus();
        int ordersInProgress = ordersInProgressCounter.countOrdersInProgress();
        boolean statusChanged = store.refreshAutomaticStatus(
                currentTime,
                ordersInProgress,
                storeCapacityPolicy
        );
        if (statusChanged) {
            var savedStore = storeRepository.save(store);
            log.info("store status changed automatically. storeId={}, oldStatus={}, newStatus={}, ordersInProgress={}",
                    savedStore.getId(), oldStatus, savedStore.getStatus(), ordersInProgress);
        }
    }

    private void refreshAutomaticStatus(Store store, LocalTime currentTime) {
        store.refreshAutomaticStatus(
                currentTime,
                ordersInProgressCounter.countOrdersInProgress(),
                storeCapacityPolicy
        );
    }

    private LocalTime currentTime() {
        return LocalTime.now(clock);
    }

    private Store findStore() {
        return storeRepository.getDefaultStore()
                .orElseThrow(StoreNotFoundException::new);
    }

    private StoreOutput toOutput(@NonNull Store store) {
        return new StoreOutput(
                store.getId(),
                store.getName(),
                store.getOpeningTime(),
                store.getClosingTime(),
                store.getMaxOrdersInProgress(),
                store.isAutomaticPause(),
                store.getStatus(),
                store.getManualStatus(),
                store.getAddressStreet(),
                store.getAddressNumber(),
                store.getAddressNeighborhood(),
                store.getAddressCity(),
                store.getAddressZipCode()
        );
    }
}
