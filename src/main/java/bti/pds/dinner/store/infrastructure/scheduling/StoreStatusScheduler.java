package bti.pds.dinner.store.infrastructure.scheduling;

import bti.pds.dinner.store.application.service.StoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class StoreStatusScheduler {

    private final StoreService storeService;

    public StoreStatusScheduler(StoreService storeService) {
        this.storeService = storeService;
    }

    @Scheduled(fixedDelayString = "${store.status.refresh-interval-ms:60000}")
    public void refreshStatuses() {
        storeService.refreshAutomaticStatuses(LocalTime.now());
    }
}
