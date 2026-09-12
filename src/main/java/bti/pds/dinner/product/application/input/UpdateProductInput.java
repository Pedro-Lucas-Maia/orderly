package bti.pds.dinner.product.application.input;

import java.math.BigDecimal;

public record UpdateProductInput(
        String name,
        String description,
        BigDecimal price,
        Boolean active
) {
}
