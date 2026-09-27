package bti.pds.dinner.store.application.service;

import bti.pds.dinner.store.application.input.CreateStoreInput;
import bti.pds.dinner.store.application.input.UpdateStoreSettingsInput;
import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreRepository;
import bti.pds.dinner.store.domain.StoreStatus;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
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
        return toOutput(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreOutput getById(Long id) {
        return toOutput(findById(id));
    }

    @Transactional
    public StoreOutput updateSettings(Long id, UpdateStoreSettingsInput input) {
        Store store = findById(id);
        store.setOpeninTime(input.openingTime());
        store.setClosingTIme(input.closingTime());
        store.setMaxOrdersInProgress(input.maxOrdersInProgress());
        store.setAutomaticPause(input.automaticPause());
        return toOutput(storeRepository.save(store));
    }

    @Transactional
    public StoreOutput updateStatusManually(Long id, UpdateStoreStatusInput input) {
        Store store = findById(id);
        store.setStatus(input.status());
        return toOutput(storeRepository.save(store));
    }

    private Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));
    }

    private StoreOutput toOutput(Store store) {
        return new StoreOutput(
                store.getId(),
                store.getName(),
                store.getOpeninTime(),
                store.getClosingTIme(),
                store.getMaxOrdersInProgress(),
                store.isAutomaticPause(),
                store.getStatus()
        );
    }
}
