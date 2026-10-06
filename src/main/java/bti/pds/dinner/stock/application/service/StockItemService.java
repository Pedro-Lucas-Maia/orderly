package bti.pds.dinner.stock.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.stock.application.input.CreateStockItemInput;
import bti.pds.dinner.stock.application.input.RegisterMovementInput;
import bti.pds.dinner.stock.application.input.UpdateStockItemInput;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.application.output.StockLotOutput;
import bti.pds.dinner.stock.domain.LotAllocation;
import bti.pds.dinner.stock.domain.LotLedger;
import bti.pds.dinner.stock.domain.MovementType;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.domain.StockLot;
import bti.pds.dinner.stock.domain.StockLotId;
import bti.pds.dinner.stock.domain.StockLotRepository;
import bti.pds.dinner.stock.domain.StockMovement;
import bti.pds.dinner.stock.domain.StockMovementRepository;
import bti.pds.dinner.stock.domain.StockRepository;
import bti.pds.dinner.stock.domain.exception.MissingLotExpiryException;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import bti.pds.dinner.stock.domain.exception.StockNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StockItemService {

    private final StockRepository stockRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockLotRepository stockLotRepository;
    private final ProductCompositionRepository productCompositionRepository;
    private final Clock clock;
    private final int adjustmentLotDays;
    private final int reversalLotDays;
    private final int initialLotDays;

    public StockItemService(
            StockRepository stockRepository,
            StockItemRepository stockItemRepository,
            StockMovementRepository stockMovementRepository,
            StockLotRepository stockLotRepository,
            ProductCompositionRepository productCompositionRepository,
            Clock clock,
            @Value("${app.stock.adjustment-lot-days:365}") int adjustmentLotDays,
            @Value("${app.stock.reversal-lot-days:7}") int reversalLotDays,
            @Value("${app.stock.initial-lot-days:90}") int initialLotDays
    ) {
        this.stockRepository = stockRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockLotRepository = stockLotRepository;
        this.productCompositionRepository = productCompositionRepository;
        this.clock = clock;
        this.adjustmentLotDays = adjustmentLotDays;
        this.reversalLotDays = reversalLotDays;
        this.initialLotDays = initialLotDays;
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

        StockItem saved = stockItemRepository.save(item);
        if (saved.getCurrentQuantity() > 0) {
            createLot(saved.getId(), saved.getCurrentQuantity(), today().plusDays(initialLotDays));
        }
        return toOutput(saved);
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

    @Transactional(readOnly = true)
    public List<StockLotOutput> listLots(Long stockItemId) {
        StockItem item = findItemOrThrow(stockItemId);
        LocalDate today = today();
        return stockLotRepository.findByStockItemId(item.getId()).stream()
                .map(lot -> toLotOutput(lot, today))
                .toList();
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
        return switch (input.type()) {
            case ENTRADA -> addStock(findItemForUpdateOrThrow(stockItemId), input.quantity(), input.reason(), requireExpiry(input));
            case SAIDA, PERDA -> deductFromItem(findActiveItemForUpdateOrThrow(stockItemId), input.type(), input.quantity(), input.reason());
            case AJUSTE -> applyAdjustment(findItemForUpdateOrThrow(stockItemId), input.quantity(), input.reason());
        };
    }

    @Transactional
    public int getBalance(Long stockItemId) {
        StockItem item = findActiveItemForUpdateOrThrow(stockItemId);
        return item.getCurrentQuantity();
    }

    @Transactional
    public void deductStock(Long stockItemId, int quantity, String reason) {
        deductFromItem(findActiveItemForUpdateOrThrow(stockItemId), MovementType.SAIDA, quantity, reason);
    }

    @Transactional
    public void addStock(Long stockItemId, int quantity, String reason) {
        addStock(
                findItemForUpdateOrThrow(stockItemId),
                quantity,
                reason,
                today().plusDays(reversalLotDays)
        );
    }

    private StockItemOutput addStock(StockItem item, int quantity, String reason, LocalDate expiresAt) {
        StockLot lot = createLot(item.getId(), quantity, expiresAt);
        StockItem updated = stockItemRepository.save(item.applyMovement(MovementType.ENTRADA, quantity));
        saveMovement(updated.getId(), MovementType.ENTRADA, quantity, reason, lot.getId());
        return toOutput(updated);
    }

    private StockItemOutput deductFromItem(StockItem item, MovementType type, int quantity, String reason) {
        consumeLots(item, quantity, type, reason);
        StockItem updated = stockItemRepository.save(item.applyMovement(type, quantity));
        return toOutput(updated);
    }

    private StockItemOutput applyAdjustment(StockItem item, int targetQuantity, String reason) {
        int current = item.getCurrentQuantity();
        if (targetQuantity > current) {
            int added = targetQuantity - current;
            StockLot lot = createLot(item.getId(), added, today().plusDays(adjustmentLotDays));
            StockItem updated = stockItemRepository.save(item.applyMovement(MovementType.AJUSTE, targetQuantity));
            saveMovement(updated.getId(), MovementType.AJUSTE, added, reason, lot.getId());
            return toOutput(updated);
        }
        if (targetQuantity < current) {
            int removed = current - targetQuantity;
            consumeLots(item, removed, MovementType.AJUSTE, reason);
            StockItem updated = stockItemRepository.save(item.applyMovement(MovementType.AJUSTE, targetQuantity));
            return toOutput(updated);
        }
        saveMovement(item.getId(), MovementType.AJUSTE, targetQuantity, reason, null);
        return toOutput(item);
    }

    private void consumeLots(StockItem item, int quantity, MovementType movementType, String reason) {
        List<StockLot> lots = stockLotRepository.findByStockItemIdForUpdate(item.getId());
        List<LotAllocation> allocations = LotLedger.allocateFefo(lots, quantity);
        Map<Long, StockLot> lotsById = lots.stream()
                .collect(Collectors.toMap(lot -> lot.getId().value(), Function.identity()));

        for (LotAllocation allocation : allocations) {
            StockLot lot = lotsById.get(allocation.lotId().value());
            StockLot consumed = lot.consume(allocation.quantity());
            if (consumed.isEmpty()) {
                stockLotRepository.delete(lot);
            } else {
                stockLotRepository.save(consumed);
            }
            saveMovement(item.getId(), movementType, allocation.quantity(), reason, allocation.lotId());
        }
    }

    private StockLot createLot(StockItemId stockItemId, int quantity, LocalDate expiresAt) {
        return stockLotRepository.save(new StockLot(
                stockItemId,
                quantity,
                expiresAt,
                LocalDateTime.now(clock)
        ));
    }

    private void saveMovement(
            StockItemId stockItemId,
            MovementType type,
            int quantity,
            String reason,
            StockLotId lotId
    ) {
        stockMovementRepository.save(new StockMovement(
                stockItemId,
                type,
                quantity,
                LocalDateTime.now(clock),
                reason,
                lotId
        ));
    }

    private LocalDate requireExpiry(RegisterMovementInput input) {
        if (input.expiresAt() == null) {
            throw new MissingLotExpiryException();
        }
        return input.expiresAt();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private StockItem findItemOrThrow(Long id) {
        StockItem item = stockItemRepository.findById(new StockItemId(id))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found"));
        if (item.isDeleted()) {
            throw new StockItemNotFoundException("Stock item not found");
        }
        return item;
    }

    private StockItem findItemForUpdateOrThrow(Long id) {
        return stockItemRepository.findByIdForUpdate(new StockItemId(id))
                .orElseThrow(() -> new StockItemNotFoundException("Stock item not found: " + id));
    }

    private StockItem findActiveItemForUpdateOrThrow(Long id) {
        StockItem item = findItemForUpdateOrThrow(id);
        if (item.isDeleted()) {
            throw new IllegalStateException("Stock item is deleted: " + id);
        }
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

    private StockLotOutput toLotOutput(StockLot lot, LocalDate today) {
        return new StockLotOutput(
                lot.getId().value(),
                lot.getStockItemId().value(),
                lot.getQuantity(),
                lot.getExpiresAt(),
                lot.getReceivedAt(),
                ChronoUnit.DAYS.between(today, lot.getExpiresAt())
        );
    }
}
