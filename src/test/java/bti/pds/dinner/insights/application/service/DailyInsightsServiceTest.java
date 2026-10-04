package bti.pds.dinner.insights.application.service;

import bti.pds.dinner.insights.application.output.DailyInsightsOutput;
import bti.pds.dinner.insights.domain.CatalogProduct;
import bti.pds.dinner.insights.domain.ConfirmedSalesHistory;
import bti.pds.dinner.insights.domain.DailySoldQuantity;
import bti.pds.dinner.insights.domain.ProductCatalog;
import bti.pds.dinner.insights.domain.RecipeCatalog;
import bti.pds.dinner.insights.domain.RecipeLine;
import bti.pds.dinner.insights.domain.StockItemSnapshot;
import bti.pds.dinner.insights.domain.StockLotSnapshot;
import bti.pds.dinner.insights.domain.StockSnapshot;
import bti.pds.dinner.insights.domain.StoreCatalog;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyInsightsServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-31T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void throwsWhenStoreDoesNotExist() {
        DailyInsightsService service = service(Optional.empty(), List.of());

        assertThrows(StoreNotFoundException.class, () -> service.getForStore(99L, null, null, null));
    }

    @Test
    void globalViewDoesNotRequireStoreId() {
        DailyInsightsService service = service(Optional.empty(), List.of(
                new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 10),
                new DailySoldQuantity(LocalDate.of(2026, 3, 10), 1L, 10),
                new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 10),
                new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 10)
        ));

        DailyInsightsOutput output = service.getGlobal(LocalDate.of(2026, 3, 31), 3, 1);

        assertNull(output.storeId());
        assertEquals(DailyInsightsService.GLOBAL_STORE_NAME, output.storeName());
        assertEquals(DailyInsightsService.DEMAND_SCOPE_ALL_STORES, output.assumptions().demandScope());
        assertEquals(10, output.productDemand().getFirst().predictedQuantity());
    }

    @Test
    void storeViewUsesStoreScopeAndClockDateWhenDateOmitted() {
        DailyInsightsService service = service(
                Optional.of(new StoreCatalog.StoreRef(1L, "Loja Principal")),
                List.of(
                        new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 10), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 10),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 10)
                )
        );

        DailyInsightsOutput output = service.getForStore(1L, null, null, null);

        assertEquals(1L, output.storeId());
        assertEquals("Loja Principal", output.storeName());
        assertEquals(LocalDate.of(2026, 3, 31), output.targetDate());
        assertEquals(DailyInsightsService.DEMAND_SCOPE_STORE, output.assumptions().demandScope());
        assertEquals(DailyInsightsService.STOCK_SCOPE_GLOBAL, output.assumptions().stockScope());
        assertEquals(10, output.productDemand().getFirst().predictedQuantity());
        assertEquals(1, output.horizonDays());
        assertEquals(3, output.expiryWindowDays());
    }

    @Test
    void wiresPurchaseCriticalAndExpiryFromStockSnapshot() {
        DailyInsightsService service = new DailyInsightsService(
                id -> Optional.of(new StoreCatalog.StoreRef(1L, "Loja Principal")),
                (storeId, from, to) -> List.of(
                        new DailySoldQuantity(LocalDate.of(2026, 3, 3), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 10), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 17), 1L, 15),
                        new DailySoldQuantity(LocalDate.of(2026, 3, 24), 1L, 15)
                ),
                () -> List.of(new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"))),
                () -> List.of(new RecipeLine(1L, 10L, 1)),
                new StockSnapshot() {
                    @Override
                    public List<StockItemSnapshot> findActiveItems() {
                        return List.of(new StockItemSnapshot(10L, "Pão", "UNIDADE", 8, 10));
                    }

                    @Override
                    public List<StockLotSnapshot> findOpenLots() {
                        return List.of(new StockLotSnapshot(42L, 10L, 8, LocalDate.of(2026, 4, 2)));
                    }
                },
                CLOCK,
                4,
                6,
                3,
                3,
                1
        );

        DailyInsightsOutput output = service.getForStore(1L, LocalDate.of(2026, 3, 31), null, null);

        assertEquals(7, output.purchaseSuggestions().getFirst().quantityToBuy());
        assertTrue(output.purchaseSuggestions().getFirst().belowMinimum());
        assertEquals(1, output.criticalStock().size());
        assertEquals(42L, output.expiringLots().getFirst().lotId());
        assertEquals(2, output.expiringLots().getFirst().daysUntilExpiry());
        assertEquals(1L, output.promotionSuggestions().getFirst().suggestedProducts().getFirst().productId());
    }

    @Test
    void sampleWeekdaysArePreviousSameWeekdays() {
        List<LocalDate> dates = DailyInsightsService.sampleWeekdays(LocalDate.of(2026, 3, 31), 4);

        assertEquals(List.of(
                LocalDate.of(2026, 3, 24),
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 3)
        ), dates);
        dates.forEach(date -> assertEquals(java.time.DayOfWeek.TUESDAY, date.getDayOfWeek()));
        assertFalse(dates.contains(LocalDate.of(2026, 3, 31)));
    }

    private static DailyInsightsService service(
            Optional<StoreCatalog.StoreRef> store,
            List<DailySoldQuantity> history
    ) {
        return new DailyInsightsService(
                id -> store.filter(ref -> ref.id().equals(id)),
                (storeId, from, to) -> history,
                () -> List.of(new CatalogProduct(1L, "Classic Burger", new BigDecimal("28.90"))),
                () -> List.of(new RecipeLine(1L, 10L, 1)),
                new StockSnapshot() {
                    @Override
                    public List<StockItemSnapshot> findActiveItems() {
                        return List.of(new StockItemSnapshot(10L, "Pão", "UNIDADE", 100, 10));
                    }

                    @Override
                    public List<StockLotSnapshot> findOpenLots() {
                        return List.of();
                    }
                },
                CLOCK,
                4,
                6,
                3,
                3,
                1
        );
    }
}
