package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.application.input.UpdateStoreSettingsInput;
import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final OrdersInProgressCounter ordersInProgressCounter;

    public StoreService(
            StoreRepository storeRepository,
            OrdersInProgressCounter ordersInProgressCounter
    ) {
        this.storeRepository = storeRepository;
        this.ordersInProgressCounter = ordersInProgressCounter;
    }

    @Transactional(readOnly = true)
    public StoreOutput getStore() {
        return toOutput(findStore());
    }

    @Transactional
    public StoreOutput updateSettings(UpdateStoreSettingsInput input) {
        Store store = findStore();
        store.updateSettings(
                input.openingTime(),
                input.closingTime(),
                input.maxOrdersInProgress(),
                input.automaticPause()
        );
        refreshAutomaticStatus(store, LocalTime.now());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public StoreOutput updateStatusManually(UpdateStoreStatusInput input) {
        Store store = findStore();
        store.setManualStatus(input.status());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public StoreOutput clearManualStatus() {
        Store store = findStore();
        store.clearManualStatus();
        refreshAutomaticStatus(store, LocalTime.now());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public void refreshAutomaticStatuses(LocalTime currentTime) {
        Store store = findStore();
        store.refreshAutomaticStatus(currentTime, ordersInProgressCounter.countOrdersInProgress());
        storeRepository.save(store);
    }

    private void refreshAutomaticStatus(Store store, LocalTime currentTime) {
        store.refreshAutomaticStatus(
                currentTime,
                ordersInProgressCounter.countOrdersInProgress()
        );
    }

    private Store findStore() {
        return storeRepository.getDefaultStore()
                .orElseThrow(() -> new StoreNotFoundException());
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
