package bti.pds.dinner.store.application.input;

import java.time.LocalTime;

public record UpdateStoreSettingsInput(
        LocalTime openingTime,
        LocalTime closingTime,
        int maxOrdersInProgress,
        boolean automaticPause
) {
}
