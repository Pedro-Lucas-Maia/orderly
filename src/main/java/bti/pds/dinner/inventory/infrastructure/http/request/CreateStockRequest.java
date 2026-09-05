package bti.pds.dinner.inventory.infrastructure.http.request;

import bti.pds.dinner.inventory.application.input.CreateStockInput;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record CreateStockRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name
) {
    public static CreateStockInput toInput(@NonNull CreateStockRequest request) {
        return new CreateStockInput(request.name());
    }
}
