package bti.pds.dinner.shoppingCart.infrastructure.http.request;

import bti.pds.dinner.shoppingCart.application.input.AddItemInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.NonNull;

public record AddItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @Positive(message = "Quantity must be positive")
        int quantity,

        String observation
) {
    public static AddItemInput from(@NonNull AddItemRequest request, String userId) {
        return new AddItemInput(
                userId,
                request.productId,
                request.quantity,
                request.observation
        );
    }
}
