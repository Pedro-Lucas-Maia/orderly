package bti.pds.dinner.product.application.output;

import java.math.BigDecimal;

public record ProductOutput(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active
) {
}
