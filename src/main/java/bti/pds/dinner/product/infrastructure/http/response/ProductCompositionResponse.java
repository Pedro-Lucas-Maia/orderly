package bti.pds.dinner.product.infrastructure.http.response;

import bti.pds.dinner.product.application.output.ProductCompositionOutput;
import org.jspecify.annotations.NonNull;

public record ProductCompositionResponse(
        Long id,
        Long productId,
        Long stockItemId,
        int quantity
) {
    public static ProductCompositionResponse from(@NonNull ProductCompositionOutput output) {
        return new ProductCompositionResponse(
                output.id(),
                output.productId(),
                output.stockItemId(),
                output.quantity()
        );
    }
}
