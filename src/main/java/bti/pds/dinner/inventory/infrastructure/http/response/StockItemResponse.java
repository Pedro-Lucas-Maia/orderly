package bti.pds.dinner.inventory.infrastructure.http.response;

import bti.pds.dinner.inventory.application.output.StockItemOutput;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record StockItemResponse(
        Long id,
        Long stockId,
        String name,
        String category,
        String unit,
        int currentQuantity,
        int minimumStock,
        BigDecimal unitCost,
        boolean active
) {
    public static StockItemResponse from(@NonNull StockItemOutput output) {
        return new StockItemResponse(
                output.id(),
                output.stockId(),
                output.name(),
                output.category(),
                output.unit(),
                output.currentQuantity(),
                output.minimumStock(),
                output.unitCost(),
                output.active()
        );
    }
}
