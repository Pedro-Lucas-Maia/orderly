package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record StockItemSnapshot(
        Long id,
        String name,
        String category,
        String unit,
        int currentQuantity,
        int minimumStock,
        BigDecimal unitCost
) {
}
