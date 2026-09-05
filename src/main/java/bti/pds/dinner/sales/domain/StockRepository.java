package bti.pds.dinner.sales.domain;

public interface StockRepository {
    int getCurrentBalance(String stockItemId);
}
