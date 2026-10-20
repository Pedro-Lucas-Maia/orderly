package bti.pds.dinner.reports.infrastructure.http.response;

import bti.pds.dinner.reports.application.output.StockReportOutput;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.List;

public record StockReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        StockReportOutput.InventoryValuation currentValuation,
        List<StockReportOutput.CategoryValuation> valuationByCategory,
        List<StockReportOutput.MovementSummary> movementsByType,
        List<StockReportOutput.ItemMovementHighlight> topConsumedItems,
        List<StockReportOutput.ItemMovementHighlight> topLostItems
) {
    public static StockReportResponse from(@NonNull StockReportOutput output) {
        return new StockReportResponse(
                output.startDate(),
                output.endDate(),
                output.currentValuation(),
                output.valuationByCategory(),
                output.movementsByType(),
                output.topConsumedItems(),
                output.topLostItems()
        );
    }
}
