package bti.pds.dinner.product.domain;

public interface StockItemRepository {
    boolean existsById(StockItemId stockItemId);

    default boolean existsById(Long stockItemId) {
        return stockItemId != null && existsById(new StockItemId(stockItemId));
    }
}
