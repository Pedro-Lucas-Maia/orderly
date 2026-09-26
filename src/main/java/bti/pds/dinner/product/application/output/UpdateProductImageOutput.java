package bti.pds.dinner.product.application.output;

import bti.pds.dinner.product.domain.Product;
import org.jspecify.annotations.NonNull;

public record UpdateProductImageOutput(
        Long productId,
        String imageUrl
) {
    public static UpdateProductImageOutput from(@NonNull Product product) {
        return new UpdateProductImageOutput(
                product.getId().value(),
                product.getImageUrl()
        );
    }
}
