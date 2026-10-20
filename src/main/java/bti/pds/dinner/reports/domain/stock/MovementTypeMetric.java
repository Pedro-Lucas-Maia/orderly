package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record MovementTypeMetric(
        String type,
        long movementsCount,
        int totalQuantity,
        BigDecimal estimatedFinancialValue
) {
}
