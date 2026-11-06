package bti.pds.dinner.reports.application.output;

import bti.pds.dinner.reports.domain.sales.DailyMetric;
import bti.pds.dinner.reports.domain.sales.ProductPerformance;
import bti.pds.dinner.reports.domain.sales.SalesSummary;
import bti.pds.dinner.reports.domain.sales.StatusMetric;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.List;

public class SalesReportOutputMapper {
    public static @NonNull SalesReportOutput map(
            LocalDate start,
            LocalDate end,
            @NonNull SalesSummary summary,
            @NonNull List<StatusMetric> statusMetrics,
            @NonNull List<DailyMetric> dailyMetrics,
            @NonNull List<ProductPerformance> productRanking
    ) {
        var summaryOutput = new SalesReportOutput.Summary(
                summary.totalOrders(),
                summary.deliveredOrders(),
                summary.canceledOrders(),
                summary.cancellationRatePercent(),
                summary.grossRevenue(),
                summary.deliveryFeeTotal(),
                summary.netProductRevenue(),
                summary.averageTicket()
        );
        var byStatusOutput = statusMetrics.stream().
                map(m -> new SalesReportOutput.StatusBreakdown(m.status(), m.orderCount(), m.totalAmount()))
                .toList();
        var dailySeriesOutput = dailyMetrics.stream()
                .map(d -> new SalesReportOutput.DailySale(d.saleDate(), d.deliveredOrders(), d.dailyRevenue()))
                .toList();
        var topProductOutput = productRanking.stream()
                .map(p -> new SalesReportOutput.ProductRanking(
                        p.productId(),
                        p.productName(),
                        p.quantitySold(),
                        p.revenueGenerated(),
                        p.revenueSharePercent()
                ))
                .toList();
        return new SalesReportOutput(
                start,
                end,
                summaryOutput,
                byStatusOutput,
                dailySeriesOutput,
                topProductOutput
        );
    }
}
