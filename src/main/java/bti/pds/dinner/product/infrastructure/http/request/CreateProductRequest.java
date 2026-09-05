package bti.pds.dinner.product.infrastructure.http.request;

import bti.pds.dinner.product.application.input.CreateProductInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price,

        boolean active
) {
    public static CreateProductInput toInput(@NonNull CreateProductRequest request) {
        return new CreateProductInput(
                request.name(),
                request.description(),
                request.price(),
                request.active()
        );
    }
}
