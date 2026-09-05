package bti.pds.dinner.product.application.service;

import bti.pds.dinner.inventory.domain.StockItemId;
import bti.pds.dinner.inventory.domain.StockItemRepository;
import bti.pds.dinner.inventory.domain.exception.StockItemNotFoundException;
import bti.pds.dinner.product.application.input.AddProductCompositionInput;
import bti.pds.dinner.product.application.output.ProductCompositionOutput;
import bti.pds.dinner.product.domain.ProductComposition;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCompositionService {
    private final ProductRepository productRepository;
    private final ProductCompositionRepository productCompositionRepository;
    private final StockItemRepository stockItemRepository;

    public ProductCompositionService(
            ProductRepository productRepository,
            ProductCompositionRepository productCompositionRepository,
            StockItemRepository stockItemRepository
    ) {
        this.productRepository = productRepository;
        this.productCompositionRepository = productCompositionRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional
    public ProductCompositionOutput add(Long productId, AddProductCompositionInput input) {
        findProductOrThrow(productId);
        stockItemRepository.findById(new StockItemId(input.stockItemId()))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found"));

        ProductComposition saved = productCompositionRepository.save(
                new ProductComposition(
                        new ProductId(productId),
                        new StockItemId(input.stockItemId()),
                        input.quantity()
                )
        );
        return toOutput(saved);
    }

    public List<ProductCompositionOutput> listByProduct(Long productId) {
        findProductOrThrow(productId);
        return productCompositionRepository.findByProductId(new ProductId(productId)).stream()
                .map(this::toOutput)
                .toList();
    }

    private void findProductOrThrow(Long productId) {
        productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private ProductCompositionOutput toOutput(ProductComposition composition) {
        return new ProductCompositionOutput(
                composition.getId().value(),
                composition.getProductId().value(),
                composition.getStockItemId().value(),
                composition.getQuantity()
        );
    }
}
