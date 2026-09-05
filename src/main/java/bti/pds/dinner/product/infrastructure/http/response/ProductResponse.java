package bti.pds.dinner.product.infrastructure.http.response;

import bti.pds.dinner.product.application.output.ProductOutput;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active
) {
    public static ProductResponse from(@NonNull ProductOutput output) {
        return new ProductResponse(
                output.id(),
                output.name(),
                output.description(),
                output.price(),
                output.active()
        );
    }
}
