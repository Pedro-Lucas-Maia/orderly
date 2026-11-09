package bti.pds.dinner.stock.domain;

import bti.pds.dinner.stock.domain.exception.InsufficientStockException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class BurgerLotAllocationStrategy implements LotAllocationStrategy {

    @Override
    public List<LotAllocation> allocate(List<StockLot> availableLots, int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        List<StockLot> ordered = availableLots.stream()
                .sorted(Comparator
                        .comparing(StockLot::getExpiresAt)
                        .thenComparing(lot -> lot.getId().value()))
                .toList();

        int remaining = requiredQuantity;
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
                    "Insufficient stock lots to allocate quantity: " + requiredQuantity
            );
        }
        return allocations;
    }
}
