package bti.pds.dinner.sales.unitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import bti.pds.dinner.sales.application.request.CreateSaleRequest;
import bti.pds.dinner.sales.application.request.SaleItemRequest;
import bti.pds.dinner.sales.application.response.SaleResponse;
import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.RecipeItem;
import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleID;
import bti.pds.dinner.sales.domain.SaleRepository;
import bti.pds.dinner.sales.domain.SaleStatus;
import bti.pds.dinner.sales.domain.StockRepository;

class SaleServiceTest {
    @Test
    void confirmsSaleAfterAggregatingAndCheckingEveryIngredient() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put("1", List.of(new RecipeItem("10", 2), new RecipeItem("11", 1)));
        products.recipes.put("2", List.of(new RecipeItem("10", 1)));
        FakeStockRepository stock = new FakeStockRepository(Map.of("10", 5, "11", 2));
        FakeSaleRepository sales = new FakeSaleRepository();

        SaleResponse response = new SaleService(sales, products, stock).execute(new CreateSaleRequest("Mesa 4", List.of(
                new SaleItemRequest("1", 2), new SaleItemRequest("2", 1))));

        assertEquals(SaleStatus.CONFIRMADA, response.status());
        assertEquals(new BigDecimal("30"), response.totalAmount());
        assertEquals(2, response.items().size());
        assertEquals(0, stock.balances.get("10"));
        assertEquals(0, stock.balances.get("11"));
        assertEquals(2, stock.deductions.size());
        assertEquals(SaleStatus.CONFIRMADA, sales.saved.getStatus());
    }

    @Test
    void doesNotDeductAnythingWhenOneIngredientIsInsufficient() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put("1", List.of(new RecipeItem("10", 1), new RecipeItem("11", 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of("10", 10, "11", 1));
        FakeSaleRepository sales = new FakeSaleRepository();

        assertThrows(IllegalStateException.class, () -> new SaleService(sales, products, stock)
                .execute(new CreateSaleRequest(null, List.of(new SaleItemRequest("1", 1)))));

        assertEquals(10, stock.balances.get("10"));
        assertEquals(1, stock.balances.get("11"));
        assertEquals(0, stock.deductions.size());
        assertEquals(null, sales.saved);
    }

    @Test
    void cancellationRestoresAllIngredientsOfConfirmedSale() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put("1", List.of(new RecipeItem("10", 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of("10", 0));
        FakeSaleRepository sales = new FakeSaleRepository();
        Sale sale = new Sale(new SaleID(), "Cliente desistiu");
        sale.addItem("1", 3, new BigDecimal("10"));
        sale.confirm();
        sales.saved = sale;

        new SaleService(sales, products, stock).cancelSale(sale.getId().uuid().toString());

        assertEquals(6, stock.balances.get("10"));
        assertEquals(SaleStatus.CANCELADA, sales.saved.getStatus());
    }

    private static class FakeProductRepository implements ProductRepository {
        private final Map<String, List<RecipeItem>> recipes = new HashMap<>();

        @Override
        public BigDecimal getCurrentPrice(String productId) {
            return new BigDecimal("10");
        }

        @Override
        public List<RecipeItem> getRecipe(String productId) {
            return recipes.getOrDefault(productId, List.of());
        }
    }

    private static class FakeStockRepository implements StockRepository {
        private final Map<String, Integer> balances = new HashMap<>();
        private final List<String> deductions = new java.util.ArrayList<>();

        FakeStockRepository(Map<String, Integer> balances) {
            this.balances.putAll(balances);
        }

        @Override
        public int getCurrentBalance(String stockItemId) {
            return balances.get(stockItemId);
        }

        @Override
        public void deductStock(String stockItemId, int quantity, String reason) {
            balances.compute(stockItemId, (id, balance) -> balance - quantity);
            deductions.add(stockItemId + ":" + quantity);
        }

        @Override
        public void addStock(String stockItemId, int quantity, String reason) {
            balances.compute(stockItemId, (id, balance) -> balance + quantity);
        }
    }

    private static class FakeSaleRepository implements SaleRepository {
        private Sale saved;

        @Override
        public void save(Sale sale) {
            saved = sale;
        }

        @Override
        public Optional<Sale> findById(SaleID id) {
            return saved != null && saved.getId().equals(id) ? Optional.of(saved) : Optional.empty();
        }

        @Override
        public List<Sale> findAll() {
            return saved == null ? List.of() : List.of(saved);
        }
    }
}
