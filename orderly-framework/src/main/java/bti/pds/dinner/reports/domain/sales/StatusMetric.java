package bti.pds.dinner.reports.domain.sales;

import java.math.BigDecimal;

public record StatusMetric(
        String status,
        long orderCount,
        BigDecimal totalAmount,
        BigDecimal deliveryFeeAmount
) {
}
