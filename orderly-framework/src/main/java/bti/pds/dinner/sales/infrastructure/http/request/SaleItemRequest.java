package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.SaleItemInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.NonNull;

public record SaleItemRequest(
        @NotNull(message = "product ID is required")
        Long productId,

        @Positive(message = "product quantity must be greater than 0")
        int quantity
) 
{
    public static SaleItemInput toInput(@NonNull SaleItemRequest request) {
        return new SaleItemInput(
                request.productId(),
                request.quantity()
        );
    }
}
