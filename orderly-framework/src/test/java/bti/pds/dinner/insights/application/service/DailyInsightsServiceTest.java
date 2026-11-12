package bti.pds.dinner.insights.application.service;

import bti.pds.dinner.insights.application.output.DailyInsightsOutput;
import bti.pds.dinner.insights.domain.*;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DailyInsightsServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-31T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void throwsWhenStoreDoesNotExist() {
        DailyInsightsService service = service(null, List.of());

        assertThrows(StoreNotFoundException.class, () -> service.getDailyInsights(null, null, null));
    }

    @Test
    void storeViewUsesStoreScopeAndClockDateWhenDateOmitted() {
        DailyInsightsService service = service(
                new StoreCatalog.StoreRef(1L, "Main Store"),
                List.of(
                        new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 10), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 10)
                )
        );

        DailyInsightsOutput output = service.getDailyInsights(null, null, null);

        assertEquals(1L, output.storeId());
        assertEquals("Main Store", output.storeName());
        assertEquals(LocalDate.of(2026, 3, 31), output.targetDate());
        assertEquals(DailyInsightsService.DEMAND_SCOPE_STORE, output.assumptions().demandScope());
        assertEquals(DailyInsightsService.STOCK_SCOPE_GLOBAL, output.assumptions().stockScope());
        assertEquals("TEST_METHOD", output.assumptions().forecastMethod());
        assertEquals(10, output.productDemand().getFirst().predictedQuantity());
        assertEquals(1, output.horizonDays());
        assertEquals(3, output.expiryWindowDays());
    }

    @Test
    void wiresPurchaseCriticalAndExpiryFromStockSnapshot() {
        DailyInsightsService service = new DailyInsightsService(
                () -> Optional.of(new StoreCatalog.StoreRef(1L, "Main Store")),
                (_, _) -> List.of(
                        new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 10), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 15)
                ),
                () -> List.of(new CatalogProduct(1L, "Product A", new BigDecimal("28.90"))),
                () -> List.of(new RecipeLine(1L, 10L, 1)),
                new StockSnapshot() {
                    @Override
                    public List<StockItemSnapshot> findActiveItems() {
                        return List.of(new StockItemSnapshot(10L, "Component A", "UNIT", 8, 10));
                    }

                    @Override
                    public List<StockLotSnapshot> findOpenLots() {
                        return List.of(new StockLotSnapshot(42L, 10L, 8, LocalDate.of(2026, 4, 2)));
                    }
                },
                new FakeDemandForecastStrategy(15, 28),
                CLOCK,
                4,
                3,
                1
        );

        DailyInsightsOutput output = service.getDailyInsights(LocalDate.of(2026, 3, 31), null, null);

        assertEquals(7, output.purchaseSuggestions().getFirst().quantityToBuy());
        assertTrue(output.purchaseSuggestions().getFirst().belowMinimum());
        assertEquals(1, output.criticalStock().size());
        assertEquals(42L, output.expiringLots().getFirst().lotId());
        assertEquals(2, output.expiringLots().getFirst().daysUntilExpiry());
        assertEquals(1L, output.promotionSuggestions().getFirst().suggestedProducts().getFirst().productId());
    }

    @Test
    void queriesSalesHistoryUsingStrategyLookbackWindowAndDelegatesForecast() {
        AtomicReference<LocalDateTime> capturedFrom = new AtomicReference<>();
        AtomicReference<LocalDateTime> capturedTo = new AtomicReference<>();

        DailyInsightsService service = new DailyInsightsService(
                () -> Optional.of(new StoreCatalog.StoreRef(1L, "Main Store")),
                (from, to) -> {
                    capturedFrom.set(from);
                    capturedTo.set(to);
                    return List.of(new DailySoldQuantity(LocalDate.of(2026, 3, 20), 1L, 12));
                },
                () -> List.of(new CatalogProduct(1L, "Product A", new BigDecimal("20.00"))),
                List::of,
                new StockSnapshot() {
                    @Override
                    public List<StockItemSnapshot> findActiveItems() {
                        return List.of();
                    }

                    @Override
                    public List<StockLotSnapshot> findOpenLots() {
                        return List.of();
                    }
                },
                new FakeDemandForecastStrategy(12, 30),
                CLOCK,
                4,
                3,
                1
        );

        LocalDate targetDate = LocalDate.of(2026, 3, 31);
        DailyInsightsOutput output = service.getDailyInsights(targetDate, null, null);

        assertEquals(LocalDate.of(2026, 3, 1).atStartOfDay(), capturedFrom.get());
        assertEquals(targetDate.atStartOfDay(), capturedTo.get());
        assertEquals(12, output.productDemand().getFirst().predictedQuantity());
        assertEquals("TEST_METHOD", output.productDemand().getFirst().method());
    }

    private static DailyInsightsService service(
            StoreCatalog.StoreRef store,
            List<DailySoldQuantity> history
    ) {
        return new DailyInsightsService(
                () -> Optional.ofNullable(store),
                (_, _) -> history,
                () -> List.of(new CatalogProduct(1L, "Product A", new BigDecimal("28.90"))),
                () -> List.of(new RecipeLine(1L, 10L, 1)),
                new StockSnapshot() {
                    @Override
                    public List<StockItemSnapshot> findActiveItems() {
                        return List.of(new StockItemSnapshot(10L, "Component A", "UNIT", 100, 10));
                    }

                    @Override
                    public List<StockLotSnapshot> findOpenLots() {
                        return List.of();
                    }
                },
                new FakeDemandForecastStrategy(10, 28),
                CLOCK,
                4,
                3,
                1
        );
    }

    private static final class FakeDemandForecastStrategy implements DemandForecastStrategy {
        private final int predictedQuantity;
        private final int lookbackDays;

        FakeDemandForecastStrategy(int predictedQuantity, int lookbackDays) {
            this.predictedQuantity = predictedQuantity;
            this.lookbackDays = lookbackDays;
        }

        @Override
        public String getMethodName() {
            return "TEST_METHOD";
        }

        @Override
        public int getHistoryLookbackDays() {
            return lookbackDays;
        }

        @Override
        public List<ProductDemand> forecast(
                List<CatalogProduct> products,
                List<DailySoldQuantity> history,
                LocalDate targetDate
        ) {
            return products.stream()
                    .map(p -> new ProductDemand(
                            p.id(),
                            p.name(),
                            p.price(),
                            predictedQuantity,
                            getMethodName(),
                            4,
                            ForecastConfidence.MEDIUM
                    ))
                    .toList();
        }
    }
}
