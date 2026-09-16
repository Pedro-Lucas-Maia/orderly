package bti.pds.dinner.product.infrastructure.http.request;

import bti.pds.dinner.product.application.input.UpdateProductInput;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Length(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        String description,

        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price,

        Boolean active
) {
    public static UpdateProductInput toInput(@NonNull UpdateProductRequest request) {
        return new UpdateProductInput(
                request.name(),
                request.description(),
                request.price(),
                request.active()
        );
    }
}
