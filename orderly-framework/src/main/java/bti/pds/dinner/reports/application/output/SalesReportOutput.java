package bti.pds.dinner.reports.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesReportOutput(
        LocalDate startDate,
        LocalDate endDate,
        Summary summary,
        List<StatusBreakdown> byStatus,
        List<DailySale> dailySeries,
        List<ProductRanking> topProducts
) {
    public record Summary(
            long totalOrders,
            long deliveredOrders,
            long canceledOrders,
            BigDecimal cancellationRatePercent,
            BigDecimal grossRevenue,
            BigDecimal deliveryFeeTotal,
            BigDecimal netProductRevenue,
            BigDecimal averageTicket
    ) {}

    public record StatusBreakdown(
            String status,
            long orderCount,
            BigDecimal totalAmount
    ) {}

    public record DailySale(
            LocalDate date,
            long deliveredOrders,
            BigDecimal revenue
    ) {}

    public record ProductRanking(
            Long productId,
            String productName,
            int quantitySold,
            BigDecimal revenueGenerated,
            BigDecimal revenueSharePercent
    ) {}
}
