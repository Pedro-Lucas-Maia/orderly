package bti.pds.dinner.reports.domain.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyMetric(
        LocalDate saleDate,
        long deliveredOrders,
        BigDecimal dailyRevenue
) {
}
