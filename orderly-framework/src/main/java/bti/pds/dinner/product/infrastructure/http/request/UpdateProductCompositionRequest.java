package bti.pds.dinner.product.infrastructure.http.request;

import bti.pds.dinner.product.application.input.UpdateProductCompositionInput;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.NonNull;

public record UpdateProductCompositionRequest(
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
    public static UpdateProductCompositionInput toInput(@NonNull UpdateProductCompositionRequest request) {
        return new UpdateProductCompositionInput(request.quantity());
    }
}
