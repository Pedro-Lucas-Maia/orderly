package bti.pds.dinner.stock.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.application.input.CreateStockInput;

public record CreateStockRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name
) {
    public static CreateStockInput toInput(@NonNull CreateStockRequest request) {
        return new CreateStockInput(request.name());
    }
}
