package bti.pds.dinner.product.infrastructure.http.controller;

import bti.pds.dinner.product.application.input.UpdateProductImageInput;
import bti.pds.dinner.product.application.service.ProductImageService;
import bti.pds.dinner.product.infrastructure.http.request.UploadImageRequest;
import bti.pds.dinner.product.infrastructure.http.response.ProductImageUploadResponse;
import bti.pds.dinner.product.infrastructure.http.response.UpdateProductImageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/api/products")
public class ProductImageController {
    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        var request = UploadImageRequest.from(file);

        var output = imageService.uploadImage(UploadImageRequest.toInput(request));

        return ProductImageUploadResponse.from(output);
    }

    @PutMapping("/{productId}/image")
    @ResponseStatus(HttpStatus.OK)
    public UpdateProductImageResponse updateProductImage(@RequestParam("file") MultipartFile file, @PathVariable Long productId) {
        var request = UploadImageRequest.from(file);
        var output = imageService.updateProductImage(new UpdateProductImageInput(
                productId,
                request.fileBytes(),
                request.fileName(),
                request.contentType(),
                request.isEmpty()
        ));
        return UpdateProductImageResponse.from(output);
    }

    @DeleteMapping("/{productId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductImage(@PathVariable Long productId) {
        imageService.deleteProductImage(productId);
    }
}
