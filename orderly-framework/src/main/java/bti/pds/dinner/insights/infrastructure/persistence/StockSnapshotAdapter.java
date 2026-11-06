package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.insights.domain.StockItemSnapshot;
import bti.pds.dinner.insights.domain.StockLotSnapshot;
import bti.pds.dinner.insights.domain.StockSnapshot;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.domain.StockLotRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockSnapshotAdapter implements StockSnapshot {
    private final StockItemRepository stockItemRepository;
    private final StockLotRepository stockLotRepository;

    public StockSnapshotAdapter(StockItemRepository stockItemRepository, StockLotRepository stockLotRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockLotRepository = stockLotRepository;
    }

    @Override
    public List<StockItemSnapshot> findActiveItems() {
        return stockItemRepository.findAll(true).stream()
                .map(item -> new StockItemSnapshot(
                        item.getId().value(),
                        item.getName(),
                        item.getUnit(),
                        item.getCurrentQuantity(),
                        item.getMinimumStock()
                ))
                .toList();
    }

    @Override
    public List<StockLotSnapshot> findOpenLots() {
        return stockLotRepository.findAll().stream()
                .filter(lot -> lot.getQuantity() > 0)
                .map(lot -> new StockLotSnapshot(
                        lot.getId().value(),
                        lot.getStockItemId().value(),
                        lot.getQuantity(),
                        lot.getExpiresAt()
                ))
                .toList();
    }
}
