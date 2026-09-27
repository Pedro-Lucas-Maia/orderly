package bti.pds.dinner.store.infrastructure.http.response;

import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.domain.StoreStatus;

import java.time.LocalTime;

public record StoreResponse(
        Long id,
        String name,
        LocalTime openingTime,
        LocalTime closingTime,
        int maxOrdersInProgress,
        boolean automaticPause,
        StoreStatus status
) {
    public static StoreResponse from(StoreOutput output) {
        return new StoreResponse(
                output.id(),
                output.name(),
                output.openingTime(),
                output.closingTime(),
                output.maxOrdersInProgress(),
                output.automaticPause(),
                output.status()
        );
    }
}
