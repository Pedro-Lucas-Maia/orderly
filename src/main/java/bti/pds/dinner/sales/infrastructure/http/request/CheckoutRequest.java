package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.CheckoutInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CheckoutRequest(
        @org.hibernate.validator.constraints.UUID(message = "Shopping Cart id must be a valid UUID", allowNil = false)
        String shoppingCartId,

        @NotNull(message = "Address id is required")
        UUID addressId,

        @NotNull(message = "Store id is required")
        @Positive(message = "Store id must be positive")
        Long storeId,

        String observation
) {
    public static CheckoutInput toInput(CheckoutRequest request, String userId) {
        return new CheckoutInput(
                UUID.fromString(userId),
                request.shoppingCartId(),
                request.addressId(),
                request.storeId(),
                request.observation()
        );
    }
}
