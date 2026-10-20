package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record ItemMovementHighlight(
        Long stockItemId,
        String stockItemName,
        String category,
        String unit,
        int totalQuantity,
        BigDecimal totalCost
) {
}
