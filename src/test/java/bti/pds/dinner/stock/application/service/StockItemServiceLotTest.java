package bti.pds.dinner.stock.application.service;

import bti.pds.dinner.product.domain.ProductComposition;
import bti.pds.dinner.product.domain.ProductCompositionId;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.stock.application.input.RegisterMovementInput;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.application.output.StockLotOutput;
import bti.pds.dinner.stock.domain.MovementType;
import bti.pds.dinner.stock.domain.Stock;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.domain.StockLot;
import bti.pds.dinner.stock.domain.StockLotId;
import bti.pds.dinner.stock.domain.StockLotRepository;
import bti.pds.dinner.stock.domain.StockMovement;
import bti.pds.dinner.stock.domain.StockMovementId;
import bti.pds.dinner.stock.domain.StockMovementRepository;
import bti.pds.dinner.stock.domain.StockRepository;
import bti.pds.dinner.stock.domain.exception.InsufficientStockException;
import bti.pds.dinner.stock.domain.exception.MissingLotExpiryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockItemServiceLotTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-31T12:00:00Z"), ZoneOffset.UTC);

    private FakeStockItemRepository items;
    private FakeStockLotRepository lots;
    private FakeStockMovementRepository movements;
    private StockItemService service;

    @BeforeEach
    void setUp() {
        items = new FakeStockItemRepository();
        lots = new FakeStockLotRepository();
        movements = new FakeStockMovementRepository();
        service = new StockItemService(
                new FakeStockRepository(),
                items,
                movements,
                lots,
                new FakeProductCompositionRepository(),
                CLOCK,
                365,
                7,
                90
        );
    }

    @Test
    void deductsFromTheEarliestLotFirst() {
        items.save(item(1L, 15));
        lots.save(lot(1L, 1L, 10, LocalDate.of(2026, 4, 1)));
        lots.save(lot(2L, 1L, 5, LocalDate.of(2026, 5, 1)));

        service.deductStock(1L, 8, "Sale #1");

        assertEquals(7, items.findById(new StockItemId(1L)).orElseThrow().getCurrentQuantity());
        List<StockLot> remaining = lots.findByStockItemId(new StockItemId(1L));
        assertEquals(2, remaining.size());
        StockLot earliest = remaining.stream().filter(lot -> lot.getId().value() == 1L).findFirst().orElseThrow();
        StockLot later = remaining.stream().filter(lot -> lot.getId().value() == 2L).findFirst().orElseThrow();
        assertEquals(2, earliest.getQuantity());
        assertEquals(5, later.getQuantity());
    }

    @Test
    void splitsDeductionAcrossLotsAndKeepsQuantityInSync() {
        items.save(item(1L, 60));
        lots.save(lot(1L, 1L, 10, LocalDate.of(2026, 4, 1)));
        lots.save(lot(2L, 1L, 50, LocalDate.of(2026, 5, 1)));

        service.deductStock(1L, 30, "Sale #2");

        assertEquals(30, items.findById(new StockItemId(1L)).orElseThrow().getCurrentQuantity());
        List<StockLot> remaining = lots.findByStockItemId(new StockItemId(1L));
        assertEquals(1, remaining.size());
        assertEquals(2L, remaining.getFirst().getId().value());
        assertEquals(30, remaining.getFirst().getQuantity());
        assertEquals(2, movements.saved.size());
        int lotSum = remaining.stream().mapToInt(StockLot::getQuantity).sum();
        assertEquals(items.findById(new StockItemId(1L)).orElseThrow().getCurrentQuantity(), lotSum);
    }

    @Test
    void doesNotChangeLotsWhenAllocationFails() {
        items.save(item(1L, 4));
        lots.save(lot(1L, 1L, 4, LocalDate.of(2026, 4, 1)));

        assertThrows(InsufficientStockException.class, () -> service.deductStock(1L, 5, "Sale #3"));

        assertEquals(4, items.findById(new StockItemId(1L)).orElseThrow().getCurrentQuantity());
        assertEquals(4, lots.findByStockItemId(new StockItemId(1L)).getFirst().getQuantity());
        assertTrue(movements.saved.isEmpty());
    }

    @Test
    void entradaRequiresExpiryDate() {
        items.save(item(1L, 0));

        assertThrows(MissingLotExpiryException.class, () -> service.registerMovement(
                1L,
                new RegisterMovementInput(MovementType.ENTRADA, 10, "Compra", null)
        ));
    }

    @Test
    void entradaCreatesLotAndIncreasesBalance() {
        items.save(item(1L, 0));

        StockItemOutput output = service.registerMovement(
                1L,
                new RegisterMovementInput(MovementType.ENTRADA, 10, "Compra", LocalDate.of(2026, 6, 1))
        );

        assertEquals(10, output.currentQuantity());
        List<StockLotOutput> listed = service.listLots(1L);
        assertEquals(1, listed.size());
        assertEquals(10, listed.getFirst().quantity());
        assertEquals(LocalDate.of(2026, 6, 1), listed.getFirst().expiresAt());
    }

    @Test
    void addStockCreatesReversalLot() {
        items.save(item(1L, 0));

        service.addStock(1L, 6, "Cancel sale #abc");

        assertEquals(6, items.findById(new StockItemId(1L)).orElseThrow().getCurrentQuantity());
        assertEquals(LocalDate.of(2026, 4, 7), lots.findByStockItemId(new StockItemId(1L)).getFirst().getExpiresAt());
    }

    private static StockItem item(Long id, int quantity) {
        return StockItem.builder()
                .id(new StockItemId(id))
                .stockId(new StockId(1L))
                .name("Pão")
                .category("Panificação")
                .unit("UNIDADE")
                .currentQuantity(quantity)
                .minimumStock(0)
                .unitCost(new BigDecimal("1.00"))
                .active(true)
                .deletedAt(null)
                .build();
    }

    private static StockLot lot(Long id, Long itemId, int quantity, LocalDate expiresAt) {
        return StockLot.builder()
                .id(new StockLotId(id))
                .stockItemId(new StockItemId(itemId))
                .quantity(quantity)
                .expiresAt(expiresAt)
                .receivedAt(LocalDate.of(2026, 3, 1).atStartOfDay())
                .build();
    }

    private static final class FakeStockRepository implements StockRepository {
        @Override
        public Stock save(Stock stock) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Stock> findById(StockId id) {
            return Optional.of(new Stock(id, "Principal"));
        }

        @Override
        public List<Stock> findAll() {
            return List.of();
        }

        @Override
        public void delete(Stock stock) {
        }
    }

    private static final class FakeStockItemRepository implements StockItemRepository {
        private final Map<Long, StockItem> store = new HashMap<>();

        @Override
        public StockItem save(StockItem stockItem) {
            store.put(stockItem.getId().value(), stockItem);
            return stockItem;
        }

        @Override
        public Optional<StockItem> findById(StockItemId id) {
            return Optional.ofNullable(store.get(id.value()));
        }

        @Override
        public Optional<StockItem> findByIdForUpdate(StockItemId id) {
            return findById(id);
        }

        @Override
        public List<StockItem> findByStockId(StockId stockId, Boolean active) {
            return List.copyOf(store.values());
        }

        @Override
        public List<StockItem> findAll(Boolean active) {
            return List.copyOf(store.values());
        }

        @Override
        public void delete(StockItem stockItem) {
            store.remove(stockItem.getId().value());
        }
    }

    private static final class FakeStockLotRepository implements StockLotRepository {
        private final Map<Long, StockLot> store = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong(100);

        @Override
        public StockLot save(StockLot stockLot) {
            StockLot persisted = stockLot.getId() == null
                    ? StockLot.builder()
                    .id(new StockLotId(sequence.incrementAndGet()))
                    .stockItemId(stockLot.getStockItemId())
                    .quantity(stockLot.getQuantity())
                    .expiresAt(stockLot.getExpiresAt())
                    .receivedAt(stockLot.getReceivedAt())
                    .build()
                    : stockLot;
            store.put(persisted.getId().value(), persisted);
            return persisted;
        }

        @Override
        public Optional<StockLot> findById(StockLotId id) {
            return Optional.ofNullable(store.get(id.value()));
        }

        @Override
        public List<StockLot> findByStockItemIdForUpdate(StockItemId stockItemId) {
            return findByStockItemId(stockItemId);
        }

        @Override
        public List<StockLot> findByStockItemId(StockItemId stockItemId) {
            return store.values().stream()
                    .filter(lot -> lot.getStockItemId().value().equals(stockItemId.value()))
                    .toList();
        }

        @Override
        public List<StockLot> findAll() {
            return List.copyOf(store.values());
        }

        @Override
        public void delete(StockLot stockLot) {
            store.remove(stockLot.getId().value());
        }
    }

    private static final class FakeStockMovementRepository implements StockMovementRepository {
        private final List<StockMovement> saved = new ArrayList<>();

        @Override
        public StockMovement save(StockMovement stockMovement) {
            saved.add(stockMovement);
            return stockMovement;
        }

        @Override
        public Optional<StockMovement> findById(StockMovementId id) {
            return Optional.empty();
        }

        @Override
        public List<StockMovement> findByStockItemId(StockItemId stockItemId) {
            return List.copyOf(saved);
        }

        @Override
        public List<StockMovement> findAll() {
            return List.copyOf(saved);
        }
    }

    private static final class FakeProductCompositionRepository implements ProductCompositionRepository {
        @Override
        public ProductComposition save(ProductComposition composition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ProductComposition> findById(ProductCompositionId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductComposition> findByProductId(ProductId productId) {
            return List.of();
        }

        @Override
        public void delete(ProductComposition composition) {
        }

        @Override
        public void deleteByProductId(ProductId productId) {
        }

        @Override
        public void deleteByStockItemId(StockItemId stockItemId) {
        }
    }
}
