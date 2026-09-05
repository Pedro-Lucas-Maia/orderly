package bti.pds.dinner.product.infrastructure.http.request;

import bti.pds.dinner.product.application.input.AddProductCompositionInput;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public record AddProductCompositionRequest(
        @NotNull(message = "Stock item id is required")
        Long stockItemId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
    public static AddProductCompositionInput toInput(@NonNull AddProductCompositionRequest request) {
        return new AddProductCompositionInput(request.stockItemId(), request.quantity());
    }
}
