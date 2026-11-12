package bti.pds.dinner.insights.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BurgerDemandForecastStrategyTest {

    @Test
    void lookbackDaysEqualsWeeksTimesSeven() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy(8, 6, 3);
        assertEquals(56, strategy.getHistoryLookbackDays());
        assertEquals(BurgerDemandForecastStrategy.METHOD, strategy.getMethodName());
    }

    @Test
    void averagesSameWeekdayIncludingZeros() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy(4, 6, 3);
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));

        LocalDate targetDate = LocalDate.of(2026, 3, 31); // Tuesday
        List<DailySoldQuantity> history = List.of(
                new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 20),
                new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 16),
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 24)
        );

        List<ProductDemand> results = strategy.forecast(List.of(burger), history, targetDate);
        ProductDemand result = results.getFirst();

        assertEquals(15, result.predictedQuantity());
        assertEquals(4, result.sampleSize());
        assertEquals(ForecastConfidence.MEDIUM, result.confidence());
        assertEquals(BurgerDemandForecastStrategy.METHOD, result.method());
    }

    @Test
    void ignoresSalesFromOtherWeekdaysAndTargetDateItself() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy(4, 6, 3);
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));

        LocalDate targetDate = LocalDate.of(2026, 3, 31); // Tuesday
        List<DailySoldQuantity> history = List.of(
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 20), // Previous Tuesday (counted)
                new DailySoldQuantity(LocalDate.of(2026, 3, 25), 1L, 50), // Wednesday (ignored)
                new DailySoldQuantity(LocalDate.of(2026, 3, 30), 1L, 40), // Monday (ignored)
                new DailySoldQuantity(LocalDate.of(2026, 3, 31), 1L, 99)  // Target date itself (ignored)
        );

        ProductDemand result = strategy.forecast(List.of(burger), history, targetDate).getFirst();

        assertEquals(5, result.predictedQuantity()); // 20 / 4 weeks = 5
        assertEquals(4, result.sampleSize());
        assertEquals(ForecastConfidence.MEDIUM, result.confidence());
    }

    @Test
    void sumsMultipleEntriesOnSameSampleWeekday() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy(2, 6, 2);
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));

        LocalDate targetDate = LocalDate.of(2026, 3, 31); // Tuesday
        List<DailySoldQuantity> history = List.of(
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 6),
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 4),
                new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 10)
        );

        ProductDemand result = strategy.forecast(List.of(burger), history, targetDate).getFirst();

        assertEquals(10, result.predictedQuantity());
        assertEquals(2, result.sampleSize());
        assertEquals(ForecastConfidence.MEDIUM, result.confidence());
    }

    @Test
    void marksHighConfidenceWhenSampleSizeMeetsThreshold() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy(6, 6, 3);
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));

        LocalDate targetDate = LocalDate.of(2026, 3, 17);
        List<LocalDate> sampleDates = List.of(
                LocalDate.of(2026, 2, 3),
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 24),
                LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 3, 10)
        );
        List<DailySoldQuantity> history = sampleDates.stream()
                .map(date -> new DailySoldQuantity(date, 1L, 10))
                .toList();

        ProductDemand result = strategy.forecast(List.of(burger), history, targetDate).getFirst();

        assertEquals(10, result.predictedQuantity());
        assertEquals(6, result.sampleSize());
        assertEquals(ForecastConfidence.HIGH, result.confidence());
    }

    @Test
    void marksUnusedProductAsLowConfidenceZero() {
        BurgerDemandForecastStrategy strategy = new BurgerDemandForecastStrategy();
        CatalogProduct extra = new CatalogProduct(2L, "Sprite", new BigDecimal("7.00"));

        ProductDemand result = strategy.forecast(List.of(extra), List.of(), LocalDate.of(2026, 3, 31)).getFirst();

        assertEquals(0, result.predictedQuantity());
        assertEquals(0, result.sampleSize());
        assertEquals(ForecastConfidence.LOW, result.confidence());
    }
}
