package bti.pds.dinner.reports.application.output;

import bti.pds.dinner.reports.domain.stock.CategoryValuation;
import bti.pds.dinner.reports.domain.stock.InventoryValuation;
import bti.pds.dinner.reports.domain.stock.ItemMovementHighlight;
import bti.pds.dinner.reports.domain.stock.MovementTypeMetric;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.List;

public class StockReportOutputMapper {
    public static @NonNull StockReportOutput map(
            LocalDate start,
            LocalDate end,
            @NonNull InventoryValuation valuation,
            @NonNull List<CategoryValuation> valuationByCategory,
            @NonNull List<MovementTypeMetric> movementTypes,
            @NonNull List<ItemMovementHighlight> topConsumed,
            @NonNull List<ItemMovementHighlight> topLost
    ) {
        var valuationOutput = new StockReportOutput.InventoryValuation(
                valuation.activeItemsCount(),
                valuation.belowMinimumCount(),
                valuation.outOfStockCount(),
                valuation.totalInventoryValue()
        );

        var categoryOutput = valuationByCategory.stream()
                .map(c -> new StockReportOutput.CategoryValuation(c.category(), c.itemsCount(), c.totalValue()))
                .toList();

        var movementsOutput = movementTypes.stream()
                .map(m -> new StockReportOutput.MovementSummary(
                        m.type(),
                        m.movementsCount(),
                        m.totalQuantity(),
                        m.estimatedFinancialValue()
                ))
                .toList();

        var topConsumedOutput = topConsumed.stream()
                .map(i -> new StockReportOutput.ItemMovementHighlight(
                        i.stockItemId(), i.stockItemName(), i.category(), i.unit(), i.totalQuantity(), i.totalCost()
                ))
                .toList();

        var topLostOutput = topLost.stream()
                .map(i -> new StockReportOutput.ItemMovementHighlight(
                        i.stockItemId(), i.stockItemName(), i.category(), i.unit(), i.totalQuantity(), i.totalCost()
                ))
                .toList();

        return new StockReportOutput(
                start,
                end,
                valuationOutput,
                categoryOutput,
                movementsOutput,
                topConsumedOutput,
                topLostOutput
        );
    }
}
