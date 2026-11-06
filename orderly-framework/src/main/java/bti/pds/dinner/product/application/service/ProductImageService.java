package bti.pds.dinner.product.application.service;

import bti.pds.dinner.product.application.input.UpdateProductImageInput;
import bti.pds.dinner.product.application.input.UploadProductImageInput;
import bti.pds.dinner.product.application.output.ProductImageOutput;
import bti.pds.dinner.product.application.output.UpdateProductImageOutput;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductImageRepository;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.domain.exception.InvalidInputException;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageService {
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    public ProductImageService(ProductRepository productRepository, ProductImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
    }

    public ProductImageOutput uploadImage(@NonNull UploadProductImageInput input) {
        validateImageFile(input.contentType(), input.isEmpty());
        String imageUrl = imageRepository.uploadImage(input.fileBytes(), input.fileName());

        return new ProductImageOutput(imageUrl);
    }

    @Transactional
    public UpdateProductImageOutput updateProductImage(@NonNull UpdateProductImageInput input) {
        validateImageFile(input.contentType(), input.isEmpty());
        var product = productRepository.findById(new ProductId(input.productId()))
                .orElseThrow(() -> new ProductNotFoundException("Product with the id " + input.productId() + " not found"));

        String imageUrl;

        if (product.getImageUrl().isBlank()) {
            imageUrl = imageRepository.uploadImage(input.fileBytes(), input.fileName());
        } else {
            imageUrl = imageRepository.replace(input.fileBytes(), input.fileName(), product.getImageUrl());
        }

        product = product.updateImage(imageUrl);

        return UpdateProductImageOutput.from(productRepository.save(product));
    }

    @Transactional
    public void deleteProductImage(@NonNull Long productId) {
        var product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new ProductNotFoundException("Product with the id " + productId + " not found"));
        imageRepository.deleteImage(product.getImageUrl());

        product.updateImage("");
    }

    private void validateImageFile(String contentType, boolean isEmpty) {
        if (isEmpty) {
            throw new InvalidInputException("Image file is required");
        }
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new InvalidInputException("Image file format is not valid, accepted formats are: jpeg, png, webp");
        }
    }
}
