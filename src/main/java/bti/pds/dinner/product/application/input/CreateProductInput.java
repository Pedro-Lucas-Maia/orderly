package bti.pds.dinner.product.application.input;

import java.math.BigDecimal;

public record CreateProductInput(
        String name,
        String description,
        BigDecimal price,
        boolean active
) {
}
