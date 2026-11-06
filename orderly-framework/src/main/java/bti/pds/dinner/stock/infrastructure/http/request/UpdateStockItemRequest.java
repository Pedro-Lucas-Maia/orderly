package bti.pds.dinner.stock.infrastructure.http.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.application.input.UpdateStockItemInput;

import java.math.BigDecimal;

public record UpdateStockItemRequest(
        @Length(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
        String name,

        @Length(min = 1, max = 255, message = "Category must be between 1 and 255 characters")
        String category,

        @Min(value = 0, message = "Minimum stock cannot be negative")
        Integer minimumStock,

        @DecimalMin(value = "0.0", inclusive = true, message = "Unit cost cannot be negative")
        BigDecimal unitCost,

        Boolean active
) {
    public static UpdateStockItemInput toInput(@NonNull UpdateStockItemRequest request) {
        return new UpdateStockItemInput(
                request.name(),
                request.category(),
                request.minimumStock(),
                request.unitCost(),
                request.active()
        );
    }
}
