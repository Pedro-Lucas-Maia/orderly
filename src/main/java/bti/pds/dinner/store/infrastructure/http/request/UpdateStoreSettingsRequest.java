package bti.pds.dinner.store.infrastructure.http.request;

import bti.pds.dinner.store.application.input.UpdateStoreSettingsInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record UpdateStoreSettingsRequest(
        @NotNull(message = "Opening time is required")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required")
        LocalTime closingTime,

        @Positive(message = "Maximum orders in progress must be greater than zero")
        int maxOrdersInProgress,

        boolean automaticPause
) {
    public static UpdateStoreSettingsInput toInput(UpdateStoreSettingsRequest request) {
        return new UpdateStoreSettingsInput(
                request.openingTime(),
                request.closingTime(),
                request.maxOrdersInProgress(),
                request.automaticPause()
        );
    }
}
