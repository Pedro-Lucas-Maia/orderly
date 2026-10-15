package bti.pds.dinner.stock.domain;

public interface ProductCompositionRepository {
    void deleteByStockItemId(StockItemId stockItemId);
}
