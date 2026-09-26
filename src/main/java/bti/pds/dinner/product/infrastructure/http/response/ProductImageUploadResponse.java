package bti.pds.dinner.product.infrastructure.http.response;

import bti.pds.dinner.product.application.output.ProductImageOutput;
import org.jspecify.annotations.NonNull;

public record ProductImageUploadResponse(
        String imageUrl
) {
    public static ProductImageUploadResponse from(@NonNull ProductImageOutput output) {
        return new ProductImageUploadResponse(
                output.imageUrl()
        );
    }
}
