package bti.pds.dinner.stock.domain;

import java.util.List;

public interface LotAllocationStrategy {
    List<LotAllocation> allocate(List<StockLot> availableLots, int requiredQuantity);
}
