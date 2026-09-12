package bti.pds.dinner.sales.infrastructure.persistence.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.sales.domain.StockRepository;
import bti.pds.dinner.stock.domain.MovementType;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockMovementEntity;
import bti.pds.dinner.stock.infrastructure.persistence.repository.StockItemEntityRepository;
import bti.pds.dinner.stock.infrastructure.persistence.repository.StockMovementEntityRepository;

@Repository
public class SalesStockRepositoryAdapter implements StockRepository {
    private final StockItemEntityRepository stockItemRepository;
    private final StockMovementEntityRepository stockMovementRepository;

    public SalesStockRepositoryAdapter(
            StockItemEntityRepository stockItemRepository,
            StockMovementEntityRepository stockMovementRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public int getCurrentBalance(String stockItemId) {
        return findActiveItemForUpdate(stockItemId).getCurrentQuantity();
    }

    @Override
    public void deductStock(String stockItemId, int quantity, String reason) {
        StockItemEntity item = findActiveItemForUpdate(stockItemId);
        if (item.getCurrentQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for item: " + stockItemId);
        }
        item.setCurrentQuantity(item.getCurrentQuantity() - quantity);
        stockItemRepository.save(item);
        registerMovement(item.getId(), MovementType.SAIDA, quantity, reason);
    }

    @Override
    public void addStock(String stockItemId, int quantity, String reason) {
        StockItemEntity item = findItemForUpdate(stockItemId);
        item.setCurrentQuantity(item.getCurrentQuantity() + quantity);
        stockItemRepository.save(item);
        registerMovement(item.getId(), MovementType.ENTRADA, quantity, reason);
    }

    private StockItemEntity findActiveItemForUpdate(String stockItemId) {
        StockItemEntity item = findItemForUpdate(stockItemId);
        if (item.getDeletedAt() != null) {
            throw new IllegalStateException("Stock item is deleted: " + stockItemId);
        }
        if (!item.isActive()) {
            throw new IllegalStateException("Stock item is inactive: " + stockItemId);
        }
        return item;
    }

    private StockItemEntity findItemForUpdate(String stockItemId) {
        Long id;
        try {
            id = Long.valueOf(stockItemId);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid stock item id: " + stockItemId, exception);
        }
        return stockItemRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock item not found: " + stockItemId));
    }

    private void registerMovement(Long stockItemId, MovementType type, int quantity, String reason) {
        stockMovementRepository.save(StockMovementEntity.builder()
                .stockItemId(stockItemId)
                .type(type)
                .quantity(quantity)
                .occurredAt(LocalDateTime.now())
                .reason(reason)
                .build());
    }
}
