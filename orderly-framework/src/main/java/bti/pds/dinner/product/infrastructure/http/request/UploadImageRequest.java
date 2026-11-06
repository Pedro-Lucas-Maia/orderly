package bti.pds.dinner.product.infrastructure.http.request;

import bti.pds.dinner.product.application.input.UploadProductImageInput;
import bti.pds.dinner.product.domain.exception.FileUnreadableException;
import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record UploadImageRequest(
        byte[] fileBytes,
        String fileName,
        String contentType,
        boolean isEmpty
) {
    public static UploadImageRequest from(@NonNull MultipartFile file) {
        try {
            return new UploadImageRequest(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.isEmpty()
            );
        } catch (IOException e) {
            throw new FileUnreadableException("The file sent could not be properly read");
        }
    }
    public static UploadProductImageInput toInput(@NonNull UploadImageRequest imageRequest) {
        return new UploadProductImageInput(
                imageRequest.fileBytes,
                imageRequest.fileName,
                imageRequest.contentType,
                imageRequest.isEmpty()
        );
    }
}
