package bti.pds.dinner.insights.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurgerCriticalLotActionStrategyTest {

    private static final String METHOD = "WEEKDAY_AVERAGE";

    @Test
    void promotionRanksProductsByPredictedDemandAndPrice() {
        BurgerCriticalLotActionStrategy strategy = new BurgerCriticalLotActionStrategy();

        var demand = List.of(
                new ProductDemand(1L, "Classic Burger", new BigDecimal("28.90"), 15, METHOD, 8, ForecastConfidence.HIGH),
                new ProductDemand(2L, "Bacon Supreme", new BigDecimal("34.90"), 8, METHOD, 8, ForecastConfidence.HIGH),
                new ProductDemand(3L, "Cheeseburger", new BigDecimal("34.90"), 15, METHOD, 8, ForecastConfidence.HIGH)
        );
        var recipes = List.of(
                new RecipeLine(1L, 3L, 2),
                new RecipeLine(2L, 3L, 1),
                new RecipeLine(3L, 3L, 1)
        );
        var items = List.of(new StockItemSnapshot(3L, "Queijo Prato", "UNIDADE", 100, 20));
        var lots = List.of(new StockLotSnapshot(42L, 3L, 30, LocalDate.of(2026, 4, 2)));

        List<InventoryAction> actions = strategy.evaluateExpiringLots(
                lots, items, recipes, demand, LocalDate.of(2026, 3, 31)
        );

        assertEquals(1, actions.size());
        assertTrue(actions.getFirst() instanceof PromotionSuggestion);
        var suggestion = (PromotionSuggestion) actions.getFirst();
        assertEquals(42L, suggestion.lotId());
        assertEquals(30, suggestion.quantityInLot());

        // Ties broken by unitPrice DESC, then productId ASC
        assertEquals(3L, suggestion.suggestedProducts().get(0).productId()); // Cheeseburger (qty 15, price 34.90)
        assertEquals(1L, suggestion.suggestedProducts().get(1).productId()); // Classic Burger (qty 15, price 28.90)
        assertEquals(2L, suggestion.suggestedProducts().get(2).productId()); // Bacon Supreme (qty 8, price 34.90)
    }

    @Test
    void expiryIgnoresLotsOutsideWindowAndZeroQuantity() {
        BurgerCriticalLotActionStrategy strategy = new BurgerCriticalLotActionStrategy();

        var demand = List.of(new ProductDemand(1L, "Classic Burger", new BigDecimal("28.90"), 15, METHOD, 8, ForecastConfidence.HIGH));
        var recipes = List.of(new RecipeLine(1L, 3L, 2));
        var items = List.of(new StockItemSnapshot(3L, "Queijo", "UNIDADE", 100, 20));
        var lots = List.of(
                new StockLotSnapshot(1L, 3L, 10, LocalDate.of(2026, 4, 10)), // outside 3-day window
                new StockLotSnapshot(2L, 3L, 0, LocalDate.of(2026, 4, 1)),   // zero quantity
                new StockLotSnapshot(3L, 3L, 5, LocalDate.of(2026, 3, 30))   // already expired / in window
        );

        List<InventoryAction> actions = strategy.evaluateExpiringLots(
                lots, items, recipes, demand, LocalDate.of(2026, 3, 31)
        );

        assertEquals(1, actions.size());
        assertTrue(actions.getFirst() instanceof PromotionSuggestion);
        var suggestion = (PromotionSuggestion) actions.getFirst();
        assertEquals(3L, suggestion.lotId());
    }

    @Test
    void handlesLotsWithItemsNotUsedInRecipes() {
        BurgerCriticalLotActionStrategy strategy = new BurgerCriticalLotActionStrategy();

        var lots = List.of(new StockLotSnapshot(10L, 99L, 12, LocalDate.of(2026, 4, 1)));
        var items = List.of(new StockItemSnapshot(99L, "Item Avulso", "UNIDADE", 12, 5));

        List<InventoryAction> actions = strategy.evaluateExpiringLots(
                lots, items, List.of(), List.of(), LocalDate.of(2026, 3, 31)
        );

        assertEquals(1, actions.size());
        assertTrue(actions.getFirst() instanceof PromotionSuggestion);
        var suggestion = (PromotionSuggestion) actions.getFirst();
        assertEquals(10L, suggestion.lotId());
        assertTrue(suggestion.suggestedProducts().isEmpty());
    }
}
