package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record CreateSaleRequest(

    @NotNull(message = "Store is required")
    Long storeId,

    String observation,

    @NotEmpty(message = "Items list cannot be empty")
    List<@Valid SaleItemRequest> items
) 
{
    public static CreateSaleInput toInput(@NonNull CreateSaleRequest request) {
        return new CreateSaleInput(
                request.storeId(),
                request.observation(),
                request.items()
                        .stream()
                        .map(SaleItemRequest::toInput)
                        .toList()
        );
    }
}
