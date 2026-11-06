package bti.pds.dinner.stock.application.output;

import java.math.BigDecimal;

public record StockItemOutput(
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
}
