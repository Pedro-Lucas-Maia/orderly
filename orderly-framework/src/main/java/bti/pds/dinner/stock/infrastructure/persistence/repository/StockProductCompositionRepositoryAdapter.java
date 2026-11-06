package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.product.application.service.ProductCompositionService;
import bti.pds.dinner.stock.domain.ProductCompositionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class StockProductCompositionRepositoryAdapter implements ProductCompositionRepository {
    private final ProductCompositionService productCompositionService;

    public StockProductCompositionRepositoryAdapter(@Lazy ProductCompositionService productCompositionService) {
        this.productCompositionService = productCompositionService;
    }

    @Override
    public void deleteByStockItemId(bti.pds.dinner.stock.domain.StockItemId stockItemId) {
        if (stockItemId != null && stockItemId.value() != null) {
           productCompositionService.deleteByStockItemId(stockItemId.value());
        }
    }
}
