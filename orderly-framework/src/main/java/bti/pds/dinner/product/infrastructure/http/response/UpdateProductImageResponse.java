package bti.pds.dinner.product.infrastructure.http.response;

import bti.pds.dinner.product.application.output.UpdateProductImageOutput;

public record UpdateProductImageResponse(
        Long productId,
        String imageUrl
) {
    public static UpdateProductImageResponse from(UpdateProductImageOutput output) {
        return new UpdateProductImageResponse(
                output.productId(),
                output.imageUrl()
        );
    }
}
