package bti.pds.dinner.store.infrastructure.http.request;

import bti.pds.dinner.store.application.input.CreateStoreInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateStoreRequest(
        @NotBlank(message = "Store name is required")
        @Size(max = 120, message = "Store name must have at most 120 characters")
        String name,

        @NotNull(message = "Opening time is required")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required")
        LocalTime closingTime,

        @Positive(message = "Maximum orders in progress must be greater than zero")
        int maxOrdersInProgress,

        boolean automaticPause
) {
    public static CreateStoreInput toInput(CreateStoreRequest request) {
        return new CreateStoreInput(
                request.name(),
                request.openingTime(),
                request.closingTime(),
                request.maxOrdersInProgress(),
                request.automaticPause()
        );
    }
}
