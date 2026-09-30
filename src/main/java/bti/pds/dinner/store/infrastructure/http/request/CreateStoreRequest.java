package bti.pds.dinner.store.infrastructure.http.request;

import bti.pds.dinner.store.application.input.CreateStoreInput;
import jakarta.validation.constraints.*;

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

        boolean automaticPause,

        @NotBlank(message = "Store street is required")
        String addressStreet,

        @NotBlank(message = "Store number is required")
        @Digits(message = "Address number must be digits", integer = 20, fraction = 0)
        String addressNumber,

        @NotBlank(message = "Store neighborhood is required")
        String addressNeighborhood,

        @NotBlank(message = "Store city is required")
        String addressCity,

        @NotBlank(message = "Store zip code is required")
        String addressZipCode
) {
    public static CreateStoreInput toInput(CreateStoreRequest request) {
        return new CreateStoreInput(
                request.name(),
                request.openingTime(),
                request.closingTime(),
                request.maxOrdersInProgress(),
                request.automaticPause(),
                request.addressStreet(),
                request.addressNumber(),
                request.addressNeighborhood(),
                request.addressCity(),
                request.addressZipCode()
        );
    }
}
