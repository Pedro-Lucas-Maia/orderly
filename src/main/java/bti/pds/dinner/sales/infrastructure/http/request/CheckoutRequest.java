package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.CheckoutInput;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record CheckoutRequest(
        @org.hibernate.validator.constraints.UUID(message = "Shopping Cart id must be a valid UUID", allowNil = false)
        String shoppingCartId,

        @NotNull(message = "Address id is required")
        UUID addressId,

        String observation
) {
    public static CheckoutInput toInput(@NonNull CheckoutRequest request, String userId) {
        return new CheckoutInput(
                UUID.fromString(userId),
                request.shoppingCartId(),
                request.addressId(),
                request.observation()
        );
    }
}
