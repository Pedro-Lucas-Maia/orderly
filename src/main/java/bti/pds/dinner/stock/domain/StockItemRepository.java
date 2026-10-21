package bti.pds.dinner.stock.domain;

import bti.pds.dinner.common.domain.PageQuery;
import bti.pds.dinner.common.domain.PagedResult;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository {
    StockItem save(StockItem stockItem);
    Optional<StockItem> findById(StockItemId id);
    Optional<StockItem> findByIdForUpdate(StockItemId id);
    PagedResult<StockItem> findByStockId(StockId stockId, StockItemFilter filter, PageQuery pageQuery);
    List<StockItem> findAll(Boolean active);
    void delete(StockItem stockItem);
}
