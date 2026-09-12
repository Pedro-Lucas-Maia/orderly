package bti.pds.dinner.sales.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.product.application.service.ProductCompositionService;
import bti.pds.dinner.product.application.service.ProductService;
import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.RecipeItem;

@Repository
public class SalesProductRepositoryAdapter implements ProductRepository {
    private final ProductService productService;
    private final ProductCompositionService productCompositionService;

    public SalesProductRepositoryAdapter(
            ProductService productService,
            ProductCompositionService productCompositionService) {
        this.productService = productService;
        this.productCompositionService = productCompositionService;
    }

    @Override
    public BigDecimal getCurrentPrice(Long productId) {
        return productService.getActiveProductPrice(productId);
    }

    @Override
    public List<RecipeItem> getRecipe(Long productId) {
        productService.getActiveProductPrice(productId);

        return productCompositionService.listByProduct(productId).stream()
                .map(output -> new RecipeItem(
                        output.stockItemId(),
                        output.quantity()))
                .toList();
    }
}
