package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;

public record CategoryValuation(
        String category,
        int itemsCount,
        BigDecimal totalValue
) {
}
