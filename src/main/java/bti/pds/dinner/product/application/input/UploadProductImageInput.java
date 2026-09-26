package bti.pds.dinner.product.application.input;

public record UploadProductImageInput(
        byte[] fileBytes,
        String fileName,
        String contentType,
        boolean isEmpty
) {
}
