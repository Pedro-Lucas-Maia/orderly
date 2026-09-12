package bti.pds.dinner.stock.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.stock.application.input.CreateStockItemInput;
import bti.pds.dinner.stock.application.input.RegisterMovementInput;
import bti.pds.dinner.stock.application.input.UpdateStockItemInput;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
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
    private final ProductCompositionRepository productCompositionRepository;

    public StockItemService(
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovementRepository stockMovementRepository,
            ProductCompositionRepository productCompositionRepository
    ) {
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productCompositionRepository = productCompositionRepository;
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

    public List<StockItemOutput> listByStock(Long stockId, Boolean active) {
        stockRepository.findById(new StockId(stockId))
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        return stockItemRepository.findByStockId(new StockId(stockId), active).stream()
                .map(this::toOutput)
                .toList();
    }

    // TODO: listagem global provisória — remover quando o front passar a usar o stockId
    public List<StockItemOutput> listAll(Boolean active) {
        return stockItemRepository.findAll(active).stream()
                .map(this::toOutput)
                .toList();
    }

    public StockItemOutput getById(Long id) {
        return toOutput(findItemOrThrow(id));
    }

    @Transactional
    public StockItemOutput update(Long id, UpdateStockItemInput input) {
        StockItem updated = findItemOrThrow(id).update(
                input.name(),
                input.category(),
                input.minimumStock(),
                input.unitCost(),
                input.active()
        );
        return toOutput(stockItemRepository.save(updated));
    }

    @Transactional
    public void delete(Long id) {
        StockItem item = findItemOrThrow(id);
        productCompositionRepository.deleteByStockItemId(item.getId());
        stockItemRepository.save(item.markDeleted());
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

    private StockItem findItemOrThrow(Long id) {
        StockItem item = stockItemRepository.findById(new StockItemId(id))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found"));
        if (item.isDeleted()) {
            throw new StockItemNotFoundException("Stock item not found");
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
