package bti.pds.dinner.sales.unitTests;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.application.service.RecipeConsumptionService;
import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.application.service.SaleStatusService;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import bti.pds.dinner.sales.domain.exception.StoreUnavailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SaleServiceTest {
    @Test
    void confirmsPendingSaleAfterAggregatingAndCheckingEveryStockComponent() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2), new RecipeItem(11L, 1)));
        products.recipes.put(2L, List.of(new RecipeItem(10L, 1)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 5, 11L, 2));
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(true, true, 0.0);

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleService service = new SaleService(sales, products, stock, () -> true, recipeConsumptionService, pricingStrategy);
        SaleStatusService statusService = new SaleStatusService(stock, sales, recipeConsumptionService, consumptionStrategy);

        SaleOutput createdSale = service.createSale(new CreateSaleInput(
                "Priority order",
                List.of(new SaleItemInput(1L, 2), new SaleItemInput(2L, 1)),
                java.util.UUID.randomUUID(),
                "Street A",
                "123",
                "City Center",
                "District A",
                "00000-000"
        ));

        assertEquals(SaleStatus.PENDENTE, createdSale.status());
        assertEquals(new BigDecimal("5.00"), createdSale.deliveryFee());
        assertEquals(new BigDecimal("35.00"), createdSale.totalAmount());
        assertEquals(0, stock.balances.get(10L));
        assertEquals(0, stock.balances.get(11L));
        assertEquals(2, stock.deductions.size());

        SaleOutput output = statusService.confirmSale(createdSale.saleId());

        assertEquals(SaleStatus.EM_PREPARO, output.status());
        assertEquals(new BigDecimal("35.00"), output.totalAmount()); // 30 of products + 5 of delivery
        assertEquals(2, output.items().size());
        assertEquals(0, stock.balances.get(10L));
        assertEquals(0, stock.balances.get(11L));
        assertEquals(2, stock.deductions.size());
        assertEquals(SaleStatus.EM_PREPARO, sales.saved.getStatus());
    }

    @Test
    void doesNotDeductAnythingWhenOneStockComponentIsInsufficient() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 1), new RecipeItem(11L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 10, 11L, 1));
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(true, true, 0.0);

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleService service = new SaleService(sales, products, stock, () -> true, recipeConsumptionService, pricingStrategy);

        assertThrows(InvalidSaleStateException.class, () -> service
                .createSale(new CreateSaleInput(
                        null,
                        List.of(new SaleItemInput(1L, 1)),
                        java.util.UUID.randomUUID(),
                        "Street A",
                        "123",
                        "City Center",
                        "District A",
                        "00000-000"
                )));

        assertEquals(10, stock.balances.get(10L));
        assertEquals(1, stock.balances.get(11L));
        assertEquals(0, stock.deductions.size());
        assertNull(sales.saved);
    }

    @Test
    void cancellationRestoresAllStockComponentsWhenPolicyAllowsRestore() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 0));
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(true, true, 0.0);

        Address address = Address.builder()
                .street("Street A")
                .number("123")
                .city("City Center")
                .neighborhood("District A")
                .zipCode("00000-000")
                .build();
        Sale sale = Sale.createNew(
                "Order cancelled by customer",
                new UserId(java.util.UUID.randomUUID()),
                address,
                List.of(new SaleItem(1L, 3, new BigDecimal("10"))),
                pricingStrategy
        );
        sale.confirm();
        sales.saved = sale;

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleStatusService statusService = new SaleStatusService(stock, sales, recipeConsumptionService, consumptionStrategy);

        statusService.cancelSale(sale.getId().uuid().toString());

        assertEquals(6, stock.balances.get(10L));
        assertEquals(SaleStatus.CANCELADA, sales.saved.getStatus());
    }

    @Test
    void cancellationDoesNotRestoreStockWhenPolicyDisallowsRestore() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 0));
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(true, false, 0.0);

        Address address = Address.builder()
                .street("Street A")
                .number("123")
                .city("City Center")
                .neighborhood("District A")
                .zipCode("00000-000")
                .build();
        Sale sale = Sale.createNew(
                "Order cancelled during preparation without stock return",
                new UserId(java.util.UUID.randomUUID()),
                address,
                List.of(new SaleItem(1L, 3, new BigDecimal("10"))),
                pricingStrategy
        );
        sale.confirm();
        sales.saved = sale;

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleStatusService statusService = new SaleStatusService(stock, sales, recipeConsumptionService, consumptionStrategy);

        statusService.cancelSale(sale.getId().uuid().toString());

        assertEquals(0, stock.balances.get(10L));
        assertEquals(SaleStatus.CANCELADA, sales.saved.getStatus());
    }

    @Test
    void cancellationThrowsWhenPolicyDisallowsCancellationForCurrentStatus() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 0));
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(false, false, 0.0);

        Address address = Address.builder()
                .street("Street A")
                .number("123")
                .city("City Center")
                .neighborhood("District A")
                .zipCode("00000-000")
                .build();
        Sale sale = Sale.createNew(
                "Order cannot be cancelled in current status",
                new UserId(java.util.UUID.randomUUID()),
                address,
                List.of(new SaleItem(1L, 3, new BigDecimal("10"))),
                pricingStrategy
        );
        sale.confirm();
        sales.saved = sale;

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleStatusService statusService = new SaleStatusService(stock, sales, recipeConsumptionService, consumptionStrategy);

        assertThrows(InvalidSaleStateException.class, () -> statusService.cancelSale(sale.getId().uuid().toString()));
        assertEquals(0, stock.balances.get(10L));
        assertEquals(SaleStatus.EM_PREPARO, sales.saved.getStatus());
    }

    @Test
    void doesNotCreateSaleWhenStoreIsNotOpen() {
        FakeProductRepository products = new FakeProductRepository();
        FakeStockRepository stock = new FakeStockRepository(Map.of());
        FakeSaleRepository sales = new FakeSaleRepository();
        FakeOrderPricingStrategy pricingStrategy = new FakeOrderPricingStrategy(new BigDecimal("5.00"));
        FakeSaleConsumptionStrategy consumptionStrategy = new FakeSaleConsumptionStrategy(true, true, 0.0);

        RecipeConsumptionService recipeConsumptionService = new RecipeConsumptionService(products, consumptionStrategy);
        SaleService service = new SaleService(sales, products, stock, () -> false, recipeConsumptionService, pricingStrategy);

        assertThrows(StoreUnavailableException.class, () -> service.createSale(new CreateSaleInput(
                null,
                List.of(new SaleItemInput(1L, 1)),
                java.util.UUID.randomUUID(),
                "Street A",
                "123",
                "City Center",
                "District A",
                "00000-000"
        )));

        assertNull(sales.saved);
    }

    private static class FakeSaleConsumptionStrategy implements SaleConsumptionStrategy {
        private final boolean cancellable;
        private final boolean restorable;
        private final double lossFactor;

        FakeSaleConsumptionStrategy(boolean cancellable, boolean restorable, double lossFactor) {
            this.cancellable = cancellable;
            this.restorable = restorable;
            this.lossFactor = lossFactor;
        }

        @Override
        public boolean canBeCancelled(SaleStatus saleStatus) {
            return cancellable;
        }

        @Override
        public boolean shouldRestoreOnCancellation(SaleStatus saleStatus) {
            return restorable;
        }

        @Override
        public double getProductionLossFactor() {
            return lossFactor;
        }
    }

    private static class FakeOrderPricingStrategy implements OrderPricingStrategy {
        private final BigDecimal flatDeliveryFee;

        FakeOrderPricingStrategy(BigDecimal flatDeliveryFee) {
            this.flatDeliveryFee = flatDeliveryFee;
        }

        @Override
        public BigDecimal calculateOrderTotal(Sale sale, List<SaleItem> items, BigDecimal baseDeliveryFee) {
            BigDecimal itemsTotal = items.stream()
                    .map(SaleItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return baseDeliveryFee != null ? itemsTotal.add(baseDeliveryFee) : itemsTotal;
        }

        @Override
        public BigDecimal calculateDeliveryFee(Address address) {
            return flatDeliveryFee;
        }
    }

    private static class FakeProductRepository implements ProductRepository {
        private final Map<Long, List<RecipeItem>> recipes = new HashMap<>();

        @Override
        public BigDecimal getCurrentPrice(Long productId) {
            return new BigDecimal("10");
        }

        @Override
        public List<RecipeItem> getRecipe(Long productId) {
            return recipes.getOrDefault(productId, List.of());
        }
    }

    private static class FakeStockRepository implements StockRepository {
        private final Map<Long, Integer> balances = new HashMap<>();
        private final List<String> deductions = new java.util.ArrayList<>();

        FakeStockRepository(Map<Long, Integer> balances) {
            this.balances.putAll(balances);
        }

        @Override
        public int getCurrentBalance(Long stockItemId) {
            return balances.get(stockItemId);
        }

        @Override
        public void deductStock(Long stockItemId, int quantity, String reason) {

            balances.compute(stockItemId, (_, balance) -> balance - quantity);
            deductions.add(stockItemId + ":" + quantity);
        }

        @Override
        public void addStock(Long stockItemId, int quantity, String reason) {
            balances.compute(stockItemId, (_, balance) -> balance + quantity);
        }
    }

    private static class FakeSaleRepository implements SaleRepository {
        private Sale saved;

        @Override
        public Sale save(Sale sale) {
            saved = sale;
            return sale;
        }

        @Override
        public Optional<Sale> findById(SaleId id) {
            return saved != null && saved.getId().equals(id) ? Optional.of(saved) : Optional.empty();
        }

        @Override
        public List<Sale> findAll(SaleStatus status, java.time.LocalDateTime date) {
            if (saved == null) return List.of();
            boolean matchStatus = status == null || saved.getStatus() == status;
            boolean matchDate = date == null || saved.getDate().isEqual(date);
            if (matchStatus && matchDate) {
                return List.of(saved);
            }
            return List.of();
        }

        @Override
        public List<Sale> findByUserId(UserId userId) {
            return saved == null ? List.of() : List.of(saved);
        }
    }
}
