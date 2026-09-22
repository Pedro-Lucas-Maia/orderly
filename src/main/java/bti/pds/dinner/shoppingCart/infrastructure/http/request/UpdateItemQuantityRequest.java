package bti.pds.dinner.shoppingCart.infrastructure.http.request;

import bti.pds.dinner.shoppingCart.application.input.UpdateItemQuantityInput;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public record UpdateItemQuantityRequest(
        @NotNull(message = "New quantity is required")
        int quantity
) {
    public static UpdateItemQuantityInput toInput(@NonNull UpdateItemQuantityRequest request, String productId, String id) {
        return new UpdateItemQuantityInput(
                id,
                productId,
                request.quantity()
        );
    }
}
