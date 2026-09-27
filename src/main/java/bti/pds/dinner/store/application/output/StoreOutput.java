package bti.pds.dinner.store.application.output;

import bti.pds.dinner.store.domain.StoreStatus;

import java.time.LocalTime;

public record StoreOutput(
        Long id,
        String name,
        LocalTime openingTime,
        LocalTime closingTime,
        int maxOrdersInProgress,
        boolean automaticPause,
        StoreStatus status,
        StoreStatus manualStatus
) {
}
