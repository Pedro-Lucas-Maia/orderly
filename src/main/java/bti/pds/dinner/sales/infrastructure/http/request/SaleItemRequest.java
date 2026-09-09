package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.SaleItemInput;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UUID;
import org.jspecify.annotations.NonNull;

public record SaleItemRequest(
        @UUID(message = "product ID must be a valid UUID")
        String productId,

        @Positive(message = "product quantity must be greater than 0")
        int quantity
) 
{
    public static SaleItemInput toInput(@NonNull SaleItemRequest request) {
        return new SaleItemInput(
                request.productId,
                request.quantity()
        );
    }
}
