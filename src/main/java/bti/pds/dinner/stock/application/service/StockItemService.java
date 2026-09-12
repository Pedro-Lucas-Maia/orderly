package bti.pds.dinner.stock.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bti.pds.dinner.stock.application.input.CreateStockItemInput;
import bti.pds.dinner.stock.application.input.RegisterMovementInput;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.domain.MovementType;
import bti.pds.dinner.stock.domain.StockMovement;
import bti.pds.dinner.stock.domain.StockMovementRepository;
import bti.pds.dinner.stock.domain.StockRepository;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import bti.pds.dinner.stock.domain.exception.StockNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockItemService {

    private final StockRepository stockRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockItemService(
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovementRepository stockMovementRepository
    ) {
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public StockItemOutput create(Long stockId, CreateStockItemInput input) {
        stockRepository.findById(new StockId(stockId))
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        StockItem item = new StockItem(
                new StockId(stockId),
                input.name(),
                input.category(),
                input.unit(),
                input.currentQuantity(),
                input.minimumStock(),
                input.unitCost(),
                input.active()
        );

        return toOutput(stockItemRepository.save(item));
    }

    public List<StockItemOutput> listByStock(Long stockId) {
        stockRepository.findById(new StockId(stockId))
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        return stockItemRepository.findByStockId(new StockId(stockId)).stream()
                .map(this::toOutput)
                .toList();
    }

    public StockItemOutput getById(Long id) {
        return toOutput(findItemOrThrow(id));
    }

    @Transactional
    public StockItemOutput registerMovement(Long stockItemId, RegisterMovementInput input) {
        StockItem item = findItemOrThrow(stockItemId);

        StockItem updated = item.applyMovement(input.type(), input.quantity());
        StockItem saved = stockItemRepository.save(updated);

        stockMovementRepository.save(new StockMovement(
                saved.getId(),
                input.type(),
                input.quantity(),
                LocalDateTime.now(),
                input.reason()
        ));

        return toOutput(saved);
    }

    @Transactional
    public int getBalance(Long stockItemId) {
        StockItem item = findActiveItemForUpdateOrThrow(stockItemId);
        return item.getCurrentQuantity();
    }

    @Transactional
    public void deductStock(Long stockItemId, int quantity, String reason) {
        StockItem item = findActiveItemForUpdateOrThrow(stockItemId);

        StockItem updated = item.applyMovement(MovementType.SAIDA, quantity);
        stockItemRepository.save(updated);

        stockMovementRepository.save(new StockMovement(
                updated.getId(),
                MovementType.SAIDA,
                quantity,
                LocalDateTime.now(),
                reason
        ));
    }

    @Transactional
    public void addStock(Long stockItemId, int quantity, String reason) {
        StockItem item = findItemForUpdateOrThrow(stockItemId);

        StockItem updated = item.applyMovement(MovementType.ENTRADA, quantity);
        stockItemRepository.save(updated);

        stockMovementRepository.save(new StockMovement(
                updated.getId(),
                MovementType.ENTRADA,
                quantity,
                LocalDateTime.now(),
                reason
        ));
    }

    private StockItem findItemOrThrow(Long id) {
        return stockItemRepository.findById(new StockItemId(id))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found"));
    }

    private StockItem findItemForUpdateOrThrow(Long id) {
        return stockItemRepository.findByIdForUpdate(new StockItemId(id))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found: " + id));
    }

    private StockItem findActiveItemForUpdateOrThrow(Long id) {
        StockItem item = findItemForUpdateOrThrow(id);
        if (!item.isActive()) {
            throw new IllegalStateException("Stock item is inactive: " + id);
        }
        return item;
    }

    private StockItemOutput toOutput(StockItem item) {
        return new StockItemOutput(
                item.getId().value(),
                item.getStockId().value(),
                item.getName(),
                item.getCategory(),
                item.getUnit(),
                item.getCurrentQuantity(),
                item.getMinimumStock(),
                item.getUnitCost(),
                item.isActive()
        );
    }
}
