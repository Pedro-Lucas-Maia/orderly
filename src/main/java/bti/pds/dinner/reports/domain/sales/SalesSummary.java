package bti.pds.dinner.reports.domain.sales;

import java.math.BigDecimal;

public record SalesSummary(
        long totalOrders,
        long deliveredOrders,
        long canceledOrders,
        BigDecimal cancellationRatePercent,
        BigDecimal grossRevenue,
        BigDecimal deliveryFeeTotal,
        BigDecimal netProductRevenue,
        BigDecimal averageTicket
) {}

