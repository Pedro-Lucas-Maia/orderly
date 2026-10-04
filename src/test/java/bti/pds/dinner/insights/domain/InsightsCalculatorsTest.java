package bti.pds.dinner.insights.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsightsCalculatorsTest {

    @Test
    void purchaseGapUsesCompositionAndHorizon() {
        var demand = List.of(new DemandForecastCalculator.ProductDemand(
                1L, "Classic Burger", new BigDecimal("28.90"), 15,
                DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH
        ));
        var recipes = List.of(new RecipeLine(1L, 10L, 1));
        var items = List.of(new StockItemSnapshot(10L, "Pão", "UNIDADE", 8, 10));

        PurchaseSuggestionCalculator.PurchaseSuggestion suggestion =
                PurchaseSuggestionCalculator.suggest(demand, recipes, items, 1).getFirst();

        assertEquals(15, suggestion.predictedConsumption());
        assertEquals(7, suggestion.quantityToBuy());
        assertTrue(suggestion.belowMinimum());
        assertEquals(List.of(1L), suggestion.affectedProductIds());
    }

    @Test
    void criticalStockListsItemsAtOrBelowMinimum() {
        var items = List.of(
                new StockItemSnapshot(10L, "Pão", "UNIDADE", 8, 10),
                new StockItemSnapshot(11L, "Carne", "UNIDADE", 40, 10)
        );
        var recipes = List.of(new RecipeLine(1L, 10L, 1));

        List<CriticalStockCalculator.CriticalStock> critical = CriticalStockCalculator.find(items, recipes);

        assertEquals(1, critical.size());
        assertEquals(10L, critical.getFirst().stockItemId());
        assertEquals(List.of(1L), critical.getFirst().affectedProductIds());
    }

    @Test
    void promotionRanksProductsByPredictedDemand() {
        var demand = List.of(
                new DemandForecastCalculator.ProductDemand(
                        1L, "Classic Burger", new BigDecimal("28.90"), 15,
                        DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH),
                new DemandForecastCalculator.ProductDemand(
                        2L, "Bacon Supreme", new BigDecimal("34.90"), 8,
                        DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH)
        );
        var recipes = List.of(new RecipeLine(1L, 3L, 2), new RecipeLine(2L, 3L, 1));
        var items = List.of(new StockItemSnapshot(3L, "Queijo", "UNIDADE", 100, 20));
        var lots = List.of(new StockLotSnapshot(42L, 3L, 30, LocalDate.of(2026, 4, 2)));

        ExpiryPromotionCalculator.Result result = ExpiryPromotionCalculator.calculate(
                LocalDate.of(2026, 3, 31), 3, lots, items, recipes, demand
        );

        assertEquals(1, result.expiringLots().size());
        assertEquals(2, result.expiringLots().getFirst().daysUntilExpiry());
        assertEquals(1L, result.promotionSuggestions().getFirst().suggestedProducts().getFirst().productId());
        assertEquals(2L, result.promotionSuggestions().getFirst().suggestedProducts().get(1).productId());
    }

    @Test
    void purchaseMultipliesConsumptionByHorizon() {
        var demand = List.of(new DemandForecastCalculator.ProductDemand(
                1L, "Classic Burger", new BigDecimal("28.90"), 15,
                DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH
        ));
        var recipes = List.of(new RecipeLine(1L, 10L, 1));
        var items = List.of(new StockItemSnapshot(10L, "Pão", "UNIDADE", 8, 10));

        PurchaseSuggestionCalculator.PurchaseSuggestion suggestion =
                PurchaseSuggestionCalculator.suggest(demand, recipes, items, 2).getFirst();

        assertEquals(30, suggestion.predictedConsumption());
        assertEquals(22, suggestion.quantityToBuy());
    }

    @Test
    void purchaseDoesNotSuggestBuyWhenStockCoversConsumption() {
        var demand = List.of(new DemandForecastCalculator.ProductDemand(
                1L, "Classic Burger", new BigDecimal("28.90"), 10,
                DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH
        ));
        var recipes = List.of(new RecipeLine(1L, 10L, 1));
        var items = List.of(new StockItemSnapshot(10L, "Pão", "UNIDADE", 40, 10));

        PurchaseSuggestionCalculator.PurchaseSuggestion suggestion =
                PurchaseSuggestionCalculator.suggest(demand, recipes, items, 1).getFirst();

        assertEquals(0, suggestion.quantityToBuy());
        assertFalse(suggestion.belowMinimum());
    }

    @Test
    void expiryIgnoresLotsOutsideWindowAndZeroQuantity() {
        var demand = List.of(new DemandForecastCalculator.ProductDemand(
                1L, "Classic Burger", new BigDecimal("28.90"), 15,
                DemandForecastCalculator.METHOD, 8, ForecastConfidence.HIGH
        ));
        var recipes = List.of(new RecipeLine(1L, 3L, 2));
        var items = List.of(new StockItemSnapshot(3L, "Queijo", "UNIDADE", 100, 20));
        var lots = List.of(
                new StockLotSnapshot(1L, 3L, 10, LocalDate.of(2026, 4, 10)),
                new StockLotSnapshot(2L, 3L, 0, LocalDate.of(2026, 4, 1)),
                new StockLotSnapshot(3L, 3L, 5, LocalDate.of(2026, 3, 30))
        );

        ExpiryPromotionCalculator.Result result = ExpiryPromotionCalculator.calculate(
                LocalDate.of(2026, 3, 31), 3, lots, items, recipes, demand
        );

        assertEquals(1, result.expiringLots().size());
        assertEquals(3L, result.expiringLots().getFirst().lotId());
        assertEquals(-1, result.expiringLots().getFirst().daysUntilExpiry());
    }
}
