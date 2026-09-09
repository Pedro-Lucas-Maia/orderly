package bti.pds.dinner.stock.infrastructure.http.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.application.input.CreateStockItemInput;

import java.math.BigDecimal;

public record CreateStockItemRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Category is required")
        String category,

        @NotBlank(message = "Unit is required")
        String unit,

        @Min(value = 0, message = "Current quantity cannot be negative")
        int currentQuantity,

        @Min(value = 0, message = "Minimum stock cannot be negative")
        int minimumStock,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Unit cost cannot be negative")
        BigDecimal unitCost,

        boolean active
) {
    public static CreateStockItemInput toInput(@NonNull CreateStockItemRequest request) {
        return new CreateStockItemInput(
                request.name(),
                request.category(),
                request.unit(),
                request.currentQuantity(),
                request.minimumStock(),
                request.unitCost(),
                request.active()
        );
    }
}
