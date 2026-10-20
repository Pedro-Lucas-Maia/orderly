package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record InventoryValuation(
        int activeItemsCount,
        int belowMinimumCount,
        int outOfStockCount,
        BigDecimal totalInventoryValue
) {
}
