package bti.pds.dinner.sales.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.sales.domain.StockRepository;
import bti.pds.dinner.stock.application.service.StockItemService;

@Repository
public class SalesStockRepositoryAdapter implements StockRepository {
    private final StockItemService stockItemService;

    public SalesStockRepositoryAdapter(StockItemService stockItemService) {
        this.stockItemService = stockItemService;
    }

    @Override
    public int getCurrentBalance(Long stockItemId) {
        return stockItemService.getBalance(stockItemId);
    }

    @Override
    public void deductStock(Long stockItemId, int quantity, String reason) {
        stockItemService.deductStock(stockItemId, quantity, reason);
    }

    @Override
    public void addStock(Long stockItemId, int quantity, String reason) {
        stockItemService.addStock(stockItemId, quantity, reason);
    }
}
