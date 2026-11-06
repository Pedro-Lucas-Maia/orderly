package bti.pds.dinner.insights.domain;

import java.math.BigDecimal;

public record CatalogProduct(
        Long id,
        String name,
        BigDecimal price
) {
}
