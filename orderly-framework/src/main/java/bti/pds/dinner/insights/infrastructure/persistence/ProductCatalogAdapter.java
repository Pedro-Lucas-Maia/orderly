package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.insights.domain.CatalogProduct;
import bti.pds.dinner.insights.domain.ProductCatalog;
import bti.pds.dinner.product.domain.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCatalogAdapter implements ProductCatalog {
    private final ProductRepository productRepository;

    public ProductCatalogAdapter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<CatalogProduct> findActiveProducts() {
        return productRepository.findAll(true).stream()
                .map(product -> new CatalogProduct(
                        product.getId().value(),
                        product.getName(),
                        product.getPrice()
                ))
                .toList();
    }
}
