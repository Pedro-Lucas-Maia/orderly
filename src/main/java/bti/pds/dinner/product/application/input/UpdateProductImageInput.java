package bti.pds.dinner.product.application.input;

public record UpdateProductImageInput(
        Long productId,
        byte[] fileBytes,
        String fileName,
        String contentType,
        boolean isEmpty
) {
}
