package bti.pds.dinner.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository {
    StockItem save(StockItem stockItem);
    Optional<StockItem> findById(StockItemId id);
    List<StockItem> findByStockId(StockId stockId);
    List<StockItem> findAll();
    void delete(StockItem stockItem);
}
