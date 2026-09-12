package bti.pds.dinner.product.application.service;

import bti.pds.dinner.product.application.input.AddProductCompositionInput;
import bti.pds.dinner.product.application.input.UpdateProductCompositionInput;
import bti.pds.dinner.product.application.output.ProductCompositionOutput;
import bti.pds.dinner.product.domain.Product;
import bti.pds.dinner.product.domain.ProductComposition;
import bti.pds.dinner.product.domain.ProductCompositionId;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.domain.exception.ProductCompositionNotFoundException;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;

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
        findStockItemOrThrow(input.stockItemId());

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

    @Transactional
    public ProductCompositionOutput update(Long productId, Long compositionId, UpdateProductCompositionInput input) {
        findProductOrThrow(productId);
        ProductComposition composition = findCompositionOrThrow(productId, compositionId);
        return toOutput(productCompositionRepository.save(composition.withQuantity(input.quantity())));
    }

    @Transactional
    public void delete(Long productId, Long compositionId) {
        findProductOrThrow(productId);
        ProductComposition composition = findCompositionOrThrow(productId, compositionId);
        productCompositionRepository.delete(composition);
    }

    private Product findProductOrThrow(Long productId) {
        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (product.isDeleted()) {
            throw new ProductNotFoundException("Product not found");
        }
        return product;
    }

    private StockItem findStockItemOrThrow(Long stockItemId) {
        StockItem item = stockItemRepository.findById(new StockItemId(stockItemId))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found"));
        if (item.isDeleted()) {
            throw new StockItemNotFoundException("Stock item not found");
        }
        return item;
    }

    private ProductComposition findCompositionOrThrow(Long productId, Long compositionId) {
        ProductComposition composition = productCompositionRepository.findById(new ProductCompositionId(compositionId))
                .orElseThrow(() -> new ProductCompositionNotFoundException("Product composition not found"));
        if (!composition.getProductId().value().equals(productId)) {
            throw new ProductCompositionNotFoundException("Product composition not found");
        }
        return composition;
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
