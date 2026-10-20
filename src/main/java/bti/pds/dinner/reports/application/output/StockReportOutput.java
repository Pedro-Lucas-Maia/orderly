package bti.pds.dinner.reports.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StockReportOutput(
        LocalDate startDate,
        LocalDate endDate,
        InventoryValuation currentValuation,
        List<CategoryValuation> valuationByCategory,
        List<MovementSummary> movementsByType,
        List<ItemMovementHighlight> topConsumedItems,
        List<ItemMovementHighlight> topLostItems
) {
    public record InventoryValuation(
            int activeItemsCount,
            int belowMinimumCount,
            int outOfStockCount,
            BigDecimal totalInventoryValue
    ) {}

    public record CategoryValuation(
            String category,
            int itemsCount,
            BigDecimal totalValue
    ) {}

    public record MovementSummary(
            String type,
            long movementsCount,
            int totalQuantity,
            BigDecimal estimatedFinancialValue
    ) {}

    public record ItemMovementHighlight(
            Long stockItemId,
            String stockItemName,
            String category,
            String unit,
            int totalQuantity,
            BigDecimal totalCost
    ) {}
}
