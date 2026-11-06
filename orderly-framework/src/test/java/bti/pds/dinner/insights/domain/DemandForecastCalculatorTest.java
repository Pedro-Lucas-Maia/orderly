package bti.pds.dinner.insights.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemandForecastCalculatorTest {

    @Test
    void averagesSameWeekdayIncludingZeros() {
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));
        List<LocalDate> dates = List.of(
                LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 24)
        );
        List<DailySoldQuantity> history = List.of(
                new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 20),
                new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 16),
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 24)
        );

        DemandForecastCalculator.ProductDemand result = DemandForecastCalculator.forecast(
                List.of(burger), dates, history, 6, 3
        ).getFirst();

        assertEquals(15, result.predictedQuantity());
        assertEquals(4, result.sampleSize());
        assertEquals(ForecastConfidence.MEDIUM, result.confidence());
        assertEquals(DemandForecastCalculator.METHOD, result.method());
    }

    @Test
    void marksHighConfidenceWhenSampleSizeMeetsThreshold() {
        CatalogProduct burger = new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"));
        List<LocalDate> dates = List.of(
                LocalDate.of(2026, 2, 3),
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 24),
                LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 3, 10)
        );
        List<DailySoldQuantity> history = dates.stream()
                .map(date -> new DailySoldQuantity(date, 1L, 10))
                .toList();

        DemandForecastCalculator.ProductDemand result = DemandForecastCalculator.forecast(
                List.of(burger), dates, history, 6, 3
        ).getFirst();

        assertEquals(10, result.predictedQuantity());
        assertEquals(6, result.sampleSize());
        assertEquals(ForecastConfidence.HIGH, result.confidence());
    }

    @Test
    void marksUnusedProductAsLowConfidenceZero() {
        CatalogProduct extra = new CatalogProduct(2L, "Sprite", new BigDecimal("7.00"));
        List<LocalDate> dates = List.of(LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 10));

        DemandForecastCalculator.ProductDemand result = DemandForecastCalculator.forecast(
                List.of(extra), dates, List.of(), 6, 3
        ).getFirst();

        assertEquals(0, result.predictedQuantity());
        assertEquals(0, result.sampleSize());
        assertEquals(ForecastConfidence.LOW, result.confidence());
    }
}
