package bti.pds.dinner.product.application.service;

import bti.pds.dinner.product.application.input.CreateProductInput;
import bti.pds.dinner.product.application.input.UpdateProductInput;
import bti.pds.dinner.product.application.output.ProductOutput;
import bti.pds.dinner.product.domain.Product;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCompositionRepository productCompositionRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductCompositionRepository productCompositionRepository
    ) {
        this.productRepository = productRepository;
        this.productCompositionRepository = productCompositionRepository;
    }

    @Transactional
    public ProductOutput create(CreateProductInput input) {
        Product saved = productRepository.save(
                new Product(input.name(), input.description(), input.price(), input.active())
        );
        return toOutput(saved);
    }

    public List<ProductOutput> list(Boolean active) {
        return productRepository.findAll(active).stream()
                .map(this::toOutput)
                .toList();
    }

    public ProductOutput getById(Long id) {
        return toOutput(findNotDeletedOrThrow(id));
    }

    @Transactional
    public ProductOutput update(Long id, UpdateProductInput input) {
        Product updated = findNotDeletedOrThrow(id).update(
                input.name(),
                input.description(),
                input.price(),
                input.active()
        );
        return toOutput(productRepository.save(updated));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findNotDeletedOrThrow(id);
        productCompositionRepository.deleteByProductId(product.getId());
        productRepository.save(product.markDeleted());
    }

    private Product findNotDeletedOrThrow(Long id) {
        Product product = productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (product.isDeleted()) {
            throw new ProductNotFoundException("Product not found");
        }
        return product;
    }

    private ProductOutput toOutput(Product product) {
        return new ProductOutput(
                product.getId().value(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );
    }
}
