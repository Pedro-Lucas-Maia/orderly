package bti.pds.dinner.stock.domain;

import bti.pds.dinner.stock.domain.exception.InsufficientStockException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LotLedger {
    private LotLedger() {
    }

    public static List<LotAllocation> allocateFefo(List<StockLot> lots, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        List<StockLot> ordered = lots.stream()
                .sorted(Comparator
                        .comparing(StockLot::getExpiresAt)
                        .thenComparing(lot -> lot.getId().value()))
                .toList();

        int remaining = quantity;
        List<LotAllocation> allocations = new ArrayList<>();
        for (StockLot lot : ordered) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(lot.getQuantity(), remaining);
            allocations.add(new LotAllocation(lot.getId(), take));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Insufficient stock lots to allocate quantity: " + quantity
            );
        }
        return allocations;
    }
}
