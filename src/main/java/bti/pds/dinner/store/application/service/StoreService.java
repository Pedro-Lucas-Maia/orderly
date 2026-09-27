package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.application.input.CreateStoreInput;
import bti.pds.dinner.store.application.input.UpdateStoreSettingsInput;
import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
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

    @Transactional
    public StoreOutput create(CreateStoreInput input) {
        Store store = new Store(
                null,
                input.name(),
                input.openingTime(),
                input.closingTime(),
                input.maxOrdersInProgress(),
                input.automaticPause(),
                StoreStatus.FECHADA
        );
        refreshAutomaticStatus(store, LocalTime.now());
        return toOutput(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreOutput getById(Long id) {
        return toOutput(findById(id));
    }

    @Transactional
    public StoreOutput updateSettings(Long id, UpdateStoreSettingsInput input) {
        Store store = findById(id);
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
    public StoreOutput updateStatusManually(Long id, UpdateStoreStatusInput input) {
        Store store = findById(id);
        store.setManualStatus(input.status());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public StoreOutput clearManualStatus(Long id) {
        Store store = findById(id);
        store.clearManualStatus();
        refreshAutomaticStatus(store, LocalTime.now());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public void refreshAutomaticStatuses(LocalTime currentTime) {
        int ordersInProgress = ordersInProgressCounter.countOrdersInProgress();
        storeRepository.findAll().stream()
                .filter(store -> store.refreshAutomaticStatus(currentTime, ordersInProgress))
                .forEach(storeRepository::save);
    }

    private void refreshAutomaticStatus(Store store, LocalTime currentTime) {
        store.refreshAutomaticStatus(currentTime, ordersInProgressCounter.countOrdersInProgress());
    }

    private Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));
    }

    private StoreOutput toOutput(Store store) {
        return new StoreOutput(
                store.getId(),
                store.getName(),
                store.getOpeningTime(),
                store.getClosingTime(),
                store.getMaxOrdersInProgress(),
                store.isAutomaticPause(),
                store.getStatus(),
                store.getManualStatus()
        );
    }
}
