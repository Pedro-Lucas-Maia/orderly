package bti.pds.dinner.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository {
    StockMovement save(StockMovement stockMovement);
    Optional<StockMovement> findById(StockMovementId id);
    List<StockMovement> findByStockItemId(StockItemId stockItemId);
    List<StockMovement> findAll();
}
