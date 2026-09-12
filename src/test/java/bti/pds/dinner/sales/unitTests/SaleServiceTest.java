package bti.pds.dinner.sales.unitTests;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SaleServiceTest {
    @Test
    void confirmsSaleAfterAggregatingAndCheckingEveryIngredient() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2), new RecipeItem(11L, 1)));
        products.recipes.put(2L, List.of(new RecipeItem(10L, 1)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 5, 11L, 2));
        FakeSaleRepository sales = new FakeSaleRepository();

        SaleOutput output = new SaleService(sales, products, stock).createSale(new CreateSaleInput("Mesa 4", List.of(
                new SaleItemInput(1L, 2), new SaleItemInput(2L, 1))));

        assertEquals(SaleStatus.CONFIRMED, output.status());
        assertEquals(new BigDecimal("30"), output.totalAmount());
        assertEquals(2, output.items().size());
        assertEquals(0, stock.balances.get(10L));
        assertEquals(0, stock.balances.get(11L));
        assertEquals(2, stock.deductions.size());
        assertEquals(SaleStatus.CONFIRMED, sales.saved.getStatus());
    }

    @Test
    void doesNotDeductAnythingWhenOneIngredientIsInsufficient() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 1), new RecipeItem(11L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 10, 11L, 1));
        FakeSaleRepository sales = new FakeSaleRepository();

        assertThrows(InvalidSaleStateException.class, () -> new SaleService(sales, products, stock)
                .createSale(new CreateSaleInput(null, List.of(new SaleItemInput(1L, 1)))));

        assertEquals(10, stock.balances.get(10L));
        assertEquals(1, stock.balances.get(11L));
        assertEquals(0, stock.deductions.size());
        assertNull(sales.saved);
    }

    @Test
    void cancellationRestoresAllIngredientsOfConfirmedSale() {
        FakeProductRepository products = new FakeProductRepository();
        products.recipes.put(1L, List.of(new RecipeItem(10L, 2)));
        FakeStockRepository stock = new FakeStockRepository(Map.of(10L, 0));
        FakeSaleRepository sales = new FakeSaleRepository();
        Sale sale = new Sale("Cliente desistiu");
        sale.addItem(1L, 3, new BigDecimal("10"));
        sale.confirm();
        sales.saved = sale;

        new SaleService(sales, products, stock).cancelSale(sale.getId().uuid().toString());

        assertEquals(6, stock.balances.get(10L));
        assertEquals(SaleStatus.CANCELLED, sales.saved.getStatus());
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
            balances.compute(stockItemId, (id, balance) -> balance - quantity);
            deductions.add(stockItemId + ":" + quantity);
        }

        @Override
        public void addStock(Long stockItemId, int quantity, String reason) {
            balances.compute(stockItemId, (id, balance) -> balance + quantity);
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
        public List<Sale> findAll() {
            return saved == null ? List.of() : List.of(saved);
        }
    }
}
