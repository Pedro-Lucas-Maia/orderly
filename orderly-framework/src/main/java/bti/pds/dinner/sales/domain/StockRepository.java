package bti.pds.dinner.sales.domain;

public interface StockRepository {
    int getCurrentBalance(Long stockItemId);
    void deductStock(Long stockItemId, int quantity, String reason);
    void addStock(Long stockItemId, int quantity, String reason);
}
