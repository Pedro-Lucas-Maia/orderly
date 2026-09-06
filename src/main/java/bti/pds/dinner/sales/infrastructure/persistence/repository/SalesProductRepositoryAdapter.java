package bti.pds.dinner.sales.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.product.domain.Product;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.RecipeItem;

@Repository
public class SalesProductRepositoryAdapter implements ProductRepository {
    private final bti.pds.dinner.product.domain.ProductRepository productRepository;
    private final ProductCompositionRepository compositionRepository;

    public SalesProductRepositoryAdapter(
            bti.pds.dinner.product.domain.ProductRepository productRepository,
            ProductCompositionRepository compositionRepository) {
        this.productRepository = productRepository;
        this.compositionRepository = compositionRepository;
    }

    @Override
    public BigDecimal getCurrentPrice(String productId) {
        return findActiveProduct(productId).getPrice();
    }

    @Override
    public List<RecipeItem> getRecipe(String productId) {
        ProductId id = new ProductId(parseId(productId, "product"));
        findActiveProduct(id);
        return compositionRepository.findByProductId(id).stream()
                .map(composition -> new RecipeItem(
                        composition.getStockItemId().value().toString(),
                        composition.getQuantity()))
                .toList();
    }

    private Product findActiveProduct(String productId) {
        return findActiveProduct(new ProductId(parseId(productId, "product")));
    }

    private Product findActiveProduct(ProductId productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId.value()));
        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive: " + productId.value());
        }
        return product;
    }

    private Long parseId(String value, String resource) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + resource + " id: " + value, exception);
        }
    }
}
