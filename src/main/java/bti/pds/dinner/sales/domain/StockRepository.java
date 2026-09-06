package bti.pds.dinner.sales.domain;

public interface StockRepository {
    int getCurrentBalance(String stockItemId);
    void deductStock(String stockItemId, int quantity, String reason);
    void addStock(String stockItemId, int quantity, String reason);
}
