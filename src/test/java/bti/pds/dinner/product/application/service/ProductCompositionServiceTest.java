package bti.pds.dinner.product.application.service;

import bti.pds.dinner.product.application.input.AddProductCompositionInput;
import bti.pds.dinner.product.application.input.UpdateProductCompositionInput;
import bti.pds.dinner.product.application.output.ProductCompositionOutput;
import bti.pds.dinner.product.domain.*;
import bti.pds.dinner.product.domain.exception.ProductNotFoundException;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProductCompositionServiceTest {

    private FakeProductRepository productRepository;
    private FakeProductCompositionRepository compositionRepository;
    private FakeStockItemRepository stockItemRepository;
    private ProductCompositionService service;

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        compositionRepository = new FakeProductCompositionRepository();
        stockItemRepository = new FakeStockItemRepository();
        service = new ProductCompositionService(productRepository, compositionRepository, stockItemRepository);
    }

    @Test
    void add_successfullyAddsComposition_whenProductAndStockItemExist() {
        Product product = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .build();
        productRepository.save(product);
        stockItemRepository.existingIds.add(10L);

        ProductCompositionOutput output = service.add(1L, new AddProductCompositionInput(10L, 2));

        assertNotNull(output);
        assertEquals(1L, output.productId());
        assertEquals(10L, output.stockItemId());
        assertEquals(2, output.quantity());
    }

    @Test
    void add_throwsProductNotFoundException_whenProductNotFound() {
        stockItemRepository.existingIds.add(10L);

        assertThrows(ProductNotFoundException.class, () ->
                service.add(99L, new AddProductCompositionInput(10L, 2))
        );
    }

    @Test
    void add_throwsProductNotFoundException_whenProductIsDeleted() {
        Product deletedProduct = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        productRepository.save(deletedProduct);
        stockItemRepository.existingIds.add(10L);

        assertThrows(ProductNotFoundException.class, () ->
                service.add(1L, new AddProductCompositionInput(10L, 2))
        );
    }

    @Test
    void add_throwsStockItemNotFoundException_whenStockItemNotFound() {
        Product product = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .build();
        productRepository.save(product);

        assertThrows(StockItemNotFoundException.class, () ->
                service.add(1L, new AddProductCompositionInput(99L, 2))
        );
    }

    @Test
    void listByProduct_returnsCompositions_whenProductExists() {
        Product product = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .build();
        productRepository.save(product);

        compositionRepository.save(new ProductComposition(new ProductCompositionId(1L), new ProductId(1L), new StockItemId(10L), 2));
        compositionRepository.save(new ProductComposition(new ProductCompositionId(2L), new ProductId(1L), new StockItemId(11L), 3));

        List<ProductCompositionOutput> list = service.listByProduct(1L);

        assertEquals(2, list.size());
    }

    @Test
    void update_updatesQuantity_whenCompositionExists() {
        Product product = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .build();
        productRepository.save(product);

        ProductComposition saved = compositionRepository.save(
                new ProductComposition(new ProductCompositionId(1L), new ProductId(1L), new StockItemId(10L), 2)
        );

        ProductCompositionOutput updated = service.update(1L, saved.getId().value(), new UpdateProductCompositionInput(5));

        assertEquals(5, updated.quantity());
    }

    @Test
    void delete_removesComposition_whenCompositionExists() {
        Product product = Product.builder()
                .id(new ProductId(1L))
                .name("Burger")
                .description("Delicious")
                .price(new BigDecimal("25.00"))
                .imageUrl("img.png")
                .active(true)
                .build();
        productRepository.save(product);

        ProductComposition saved = compositionRepository.save(
                new ProductComposition(new ProductCompositionId(1L), new ProductId(1L), new StockItemId(10L), 2)
        );

        service.delete(1L, saved.getId().value());

        assertTrue(compositionRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteByStockItemId_removesCompositions_withMatchingStockItemId() {
        compositionRepository.save(
                new ProductComposition(new ProductCompositionId(1L), new ProductId(1L), new StockItemId(10L), 2)
        );

        service.deleteByStockItemId(10L);

        assertTrue(compositionRepository.findById(new ProductCompositionId(1L)).isEmpty());
    }

    private static class FakeProductRepository implements ProductRepository {
        private final Map<Long, Product> map = new HashMap<>();

        @Override
        public Product save(Product product) {
            map.put(product.getId().value(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(map.get(id.value()));
        }

        @Override
        public List<Product> findAll(Boolean active) {
            return new ArrayList<>(map.values());
        }

        @Override
        public void delete(Product product) {
            map.remove(product.getId().value());
        }
    }

    private static class FakeProductCompositionRepository implements ProductCompositionRepository {
        private final Map<Long, ProductComposition> map = new HashMap<>();
        private long seq = 1;

        @Override
        public ProductComposition save(ProductComposition productComposition) {
            ProductCompositionId id = productComposition.getId() != null ? productComposition.getId() : new ProductCompositionId(seq++);
            ProductComposition toSave = new ProductComposition(id, productComposition.getProductId(), productComposition.getStockItemId(), productComposition.getQuantity());
            map.put(id.value(), toSave);
            return toSave;
        }

        @Override
        public Optional<ProductComposition> findById(ProductCompositionId id) {
            return Optional.ofNullable(map.get(id.value()));
        }

        @Override
        public List<ProductComposition> findByProductId(ProductId productId) {
            return map.values().stream()
                    .filter(c -> c.getProductId().value().equals(productId.value()))
                    .toList();
        }

        @Override
        public void delete(ProductComposition productComposition) {
            map.remove(productComposition.getId().value());
        }

        @Override
        public void deleteByProductId(ProductId productId) {
            map.values().removeIf(c -> c.getProductId().value().equals(productId.value()));
        }

        @Override
        public void deleteByStockItemId(StockItemId stockItemId) {
            map.values().removeIf(c -> c.getStockItemId().value().equals(stockItemId.value()));
        }
    }

    private static class FakeStockItemRepository implements StockItemRepository {
        final Set<Long> existingIds = new HashSet<>();

        @Override
        public boolean existsById(StockItemId stockItemId) {
            if (stockItemId == null || stockItemId.value() == null || !existingIds.contains(stockItemId.value())) {
                throw new StockItemNotFoundException("Stock item not found");
            }
            return true;
        }
    }
}
