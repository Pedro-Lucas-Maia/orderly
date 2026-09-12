package bti.pds.dinner.product.application.service;

import bti.pds.dinner.product.application.input.CreateProductInput;
import bti.pds.dinner.product.application.output.ProductOutput;
import bti.pds.dinner.product.domain.Product;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductOutput create(CreateProductInput input) {
        Product saved = productRepository.save(
                new Product(input.name(), input.description(), input.price(), input.active())
        );
        return toOutput(saved);
    }

    public List<ProductOutput> list() {
        return productRepository.findAll().stream()
                .map(this::toOutput)
                .toList();
    }

    public ProductOutput getById(Long id) {
        Product product = productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return toOutput(product);
    }

    public BigDecimal getActiveProductPrice(Long id) {
        return findActiveProduct(id).getPrice();
    }

    private Product findActiveProduct(Long id) {
        Product product = productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive: " + id);
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
