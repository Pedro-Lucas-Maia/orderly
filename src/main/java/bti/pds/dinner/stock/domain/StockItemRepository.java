package bti.pds.dinner.stock.domain;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository {
    StockItem save(StockItem stockItem);
    Optional<StockItem> findById(StockItemId id);
    List<StockItem> findByStockId(StockId stockId, Boolean active);
    List<StockItem> findAll(Boolean active);
    void delete(StockItem stockItem);
}
