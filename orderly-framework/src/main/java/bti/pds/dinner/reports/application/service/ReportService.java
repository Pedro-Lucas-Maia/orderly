package bti.pds.dinner.reports.application.service;

import bti.pds.dinner.reports.application.input.ReportPeriodInput;
import bti.pds.dinner.reports.application.output.SalesReportOutput;
import bti.pds.dinner.reports.application.output.SalesReportOutputMapper;
import bti.pds.dinner.reports.application.output.StockReportOutput;
import bti.pds.dinner.reports.application.output.StockReportOutputMapper;
import bti.pds.dinner.reports.domain.InvalidReportPeriodException;
import bti.pds.dinner.reports.domain.sales.*;
import bti.pds.dinner.reports.domain.stock.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private static final int DEFAULT_PERIOD_DAYS = 30;

    private final SalesReportDataPort salesDataPort;
    private final StockReportDataPort stockDataPort;
    private final SalesReportCalculator salesCalculator;
    private final StockReportCalculator stockCalculator;

    public ReportService(SalesReportDataPort salesDataPort, StockReportDataPort stockDataPort) {
        this.salesDataPort = salesDataPort;
        this.stockDataPort = stockDataPort;
        this.salesCalculator = new SalesReportCalculator();
        this.stockCalculator = new StockReportCalculator();
    }

    @Transactional(readOnly = true)
    public SalesReportOutput generateSalesReport(ReportPeriodInput input) {
        LocalDate end = resolveEndDate(input.endDate());
        LocalDate start = resolveStartDate(input.startDate(), end);
        validatePeriod(start, end);

        LocalDateTime fromInclusive = start.atStartOfDay();
        LocalDateTime toExclusive = end.plusDays(1).atStartOfDay();

        List<StatusMetric> statusMetrics = salesDataPort.findStatusMetrics(fromInclusive, toExclusive);
        List<DailyMetric> dailyMetrics = salesDataPort.findDailyMetrics(fromInclusive, toExclusive);
        List<ProductMetric> productMetrics = salesDataPort.findProductMetrics(fromInclusive, toExclusive);

        SalesSummary summary = salesCalculator.summarize(statusMetrics);
        List<ProductPerformance> productRanking = salesCalculator.calculateProductRanking(productMetrics);

        return SalesReportOutputMapper.map(start, end, summary, statusMetrics, dailyMetrics, productRanking);
    }

    @Transactional(readOnly = true)
    public StockReportOutput generateStockReport(@NonNull ReportPeriodInput input) {
        LocalDate end = resolveEndDate(input.endDate());
        LocalDate start = resolveStartDate(input.startDate(), end);
        validatePeriod(start, end);

        LocalDateTime fromInclusive = start.atStartOfDay();
        LocalDateTime toExclusive = end.plusDays(1).atStartOfDay();

        List<StockItemSnapshot> activeItems = stockDataPort.findActiveStockItems();
        List<MovementTypeMetric> movementTypes = stockDataPort.findMovementTypeMetrics(fromInclusive, toExclusive);
        List<ItemMovementMetric> itemMovements = stockDataPort.findItemMovementMetrics(fromInclusive, toExclusive);

        InventoryValuation valuation = stockCalculator.calculateValuation(activeItems);
        List<CategoryValuation> valuationByCategory = stockCalculator.calculateValuationByCategory(activeItems);
        List<ItemMovementHighlight> topConsumed = stockCalculator.filterConsumedItems(itemMovements);
        List<ItemMovementHighlight> topLost = stockCalculator.filterLostItems(itemMovements);

        return StockReportOutputMapper.map(start, end, valuation, valuationByCategory, movementTypes, topConsumed, topLost);
    }

    private LocalDate resolveEndDate(LocalDate endDate) {
        return endDate != null ? endDate : LocalDate.now();
    }

    private LocalDate resolveStartDate(LocalDate startDate, LocalDate resolvedEndDate) {
        return startDate != null ? startDate : resolvedEndDate.minusDays(DEFAULT_PERIOD_DAYS - 1);
    }

    private void validatePeriod(@NonNull LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new InvalidReportPeriodException("startDate (" + start + ") cannot be after endDate (" + end + ")");
        }
    }
}
