package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record ItemMovementMetric(
        Long stockItemId,
        String stockItemName,
        String category,
        String unit,
        String type,
        int totalQuantity,
        BigDecimal totalCost
) {
}
