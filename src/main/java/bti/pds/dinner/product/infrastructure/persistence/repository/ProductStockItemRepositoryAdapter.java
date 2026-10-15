package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.domain.StockItemId;
import bti.pds.dinner.product.domain.StockItemRepository;
import bti.pds.dinner.stock.application.service.StockItemService;
import org.springframework.stereotype.Repository;

@Repository
public class ProductStockItemRepositoryAdapter implements StockItemRepository {
    private final StockItemService stockItemService;

    public ProductStockItemRepositoryAdapter(StockItemService stockItemService) {
        this.stockItemService = stockItemService;
    }

    @Override
    public boolean existsById(StockItemId stockItemId) {
        return stockItemService.getById(stockItemId.value()) != null;
    }
}
