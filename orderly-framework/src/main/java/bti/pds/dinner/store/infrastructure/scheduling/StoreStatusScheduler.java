package bti.pds.dinner.store.infrastructure.scheduling;

import bti.pds.dinner.store.application.service.StoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
public class StoreStatusScheduler {

    private final StoreService storeService;
    private final Clock clock;

    public StoreStatusScheduler(StoreService storeService, Clock clock) {
        this.storeService = storeService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${store.status.refresh-interval-ms:60000}")
    public void refreshStatuses() {
        storeService.refreshAutomaticStatuses(LocalTime.now(clock));
    }
}
