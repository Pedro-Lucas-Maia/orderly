package bti.pds.dinner.sales.infrastructure.http.request;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record CreateSaleRequest(

    String observation,

    @NotEmpty(message = "Items list cannot be empty")
    List<@Valid SaleItemRequest> items
) 
{
    public static CreateSaleInput toInput(@NonNull CreateSaleRequest request) {
        return new CreateSaleInput(
                request.observation(),
                request.items
                        .stream()
                        .map(SaleItemRequest::toInput)
                        .toList()
        );
    }
}
