package bti.pds.dinner.stock.infrastructure.http.response;

import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.application.output.StockItemOutput;

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
