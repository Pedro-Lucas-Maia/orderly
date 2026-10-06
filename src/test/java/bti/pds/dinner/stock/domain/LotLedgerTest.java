package bti.pds.dinner.stock.domain;

import bti.pds.dinner.stock.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LotLedgerTest {

    @Test
    void consumesOnlyTheEarliestLotWhenQuantityFits() {
        StockLot sooner = lot(1L, 10, LocalDate.of(2026, 4, 1));
        StockLot later = lot(2L, 50, LocalDate.of(2026, 5, 1));

        List<LotAllocation> allocations = LotLedger.allocateFefo(List.of(later, sooner), 8);

        assertEquals(1, allocations.size());
        assertEquals(1L, allocations.getFirst().lotId().value());
        assertEquals(8, allocations.getFirst().quantity());
    }

    @Test
    void splitsAcrossLotsInExpiryOrder() {
        StockLot sooner = lot(1L, 10, LocalDate.of(2026, 4, 1));
        StockLot later = lot(2L, 50, LocalDate.of(2026, 5, 1));

        List<LotAllocation> allocations = LotLedger.allocateFefo(List.of(later, sooner), 30);

        assertEquals(2, allocations.size());
        assertEquals(1L, allocations.get(0).lotId().value());
        assertEquals(10, allocations.get(0).quantity());
        assertEquals(2L, allocations.get(1).lotId().value());
        assertEquals(20, allocations.get(1).quantity());
    }

    @Test
    void usesLotIdAsTieBreakerWhenExpiryMatches() {
        StockLot first = lot(1L, 5, LocalDate.of(2026, 4, 1));
        StockLot second = lot(2L, 5, LocalDate.of(2026, 4, 1));

        List<LotAllocation> allocations = LotLedger.allocateFefo(List.of(second, first), 6);

        assertEquals(1L, allocations.get(0).lotId().value());
        assertEquals(5, allocations.get(0).quantity());
        assertEquals(2L, allocations.get(1).lotId().value());
        assertEquals(1, allocations.get(1).quantity());
    }

    @Test
    void throwsWhenLotsCannotCoverQuantity() {
        StockLot lot = lot(1L, 4, LocalDate.of(2026, 4, 1));

        assertThrows(InsufficientStockException.class, () -> LotLedger.allocateFefo(List.of(lot), 5));
    }

    private static StockLot lot(Long id, int quantity, LocalDate expiresAt) {
        return StockLot.builder()
                .id(new StockLotId(id))
                .stockItemId(new StockItemId(10L))
                .quantity(quantity)
                .expiresAt(expiresAt)
                .receivedAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .build();
    }
}
