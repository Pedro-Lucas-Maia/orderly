package bti.pds.dinner.reports.domain.sales;

import java.math.BigDecimal;

public record ProductPerformance(
        Long productId,
        String productName,
        int quantitySold,
        BigDecimal revenueGenerated,
        BigDecimal revenueSharePercent
) {
}
