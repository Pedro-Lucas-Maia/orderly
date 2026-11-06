package bti.pds.dinner.stock.domain;

import java.util.List;
import java.util.Optional;

public interface StockLotRepository {
    StockLot save(StockLot stockLot);
    Optional<StockLot> findById(StockLotId id);
    List<StockLot> findByStockItemIdForUpdate(StockItemId stockItemId);
    List<StockLot> findByStockItemId(StockItemId stockItemId);
    List<StockLot> findAll();
    void delete(StockLot stockLot);
}
