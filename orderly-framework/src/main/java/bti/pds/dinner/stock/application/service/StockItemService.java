package bti.pds.dinner.stock.application.service;

import bti.pds.dinner.common.domain.PageQuery;
import bti.pds.dinner.common.domain.PagedResult;
import bti.pds.dinner.stock.application.input.CreateStockItemInput;
import bti.pds.dinner.stock.application.input.ListStockItemsInput;
import bti.pds.dinner.stock.application.input.RegisterMovementInput;
import bti.pds.dinner.stock.application.input.UpdateStockItemInput;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.application.output.StockLotOutput;
import bti.pds.dinner.stock.domain.*;
import bti.pds.dinner.stock.domain.exception.MissingLotExpiryException;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import bti.pds.dinner.stock.domain.exception.StockNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockItemService {

    private final StockRepository stockRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockLotRepository stockLotRepository;
    private final ProductCompositionRepository productCompositionRepository;
    private final LotAllocationStrategy lotAllocationStrategy;
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
            LotAllocationStrategy lotAllocationStrategy,
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
        this.lotAllocationStrategy = lotAllocationStrategy;
        this.clock = clock;
        this.adjustmentLotDays = adjustmentLotDays;
        this.reversalLotDays = reversalLotDays;
        this.initialLotDays = initialLotDays;
    }

    @Transactional
    @Observed(name = "stock-item.admin.create")
    public StockItemOutput create(Long stockId, @NonNull CreateStockItemInput input) {
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
        log.info("stock item created. name={}, stockItemId={}", saved.getName(), saved.getId().value());
        return toOutput(saved);
    }

    public PagedResult<StockItemOutput> listByStock(Long stockId, ListStockItemsInput input) {
        stockRepository.findById(new StockId(stockId))
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        var filter = new StockItemFilter(input.name(), input.active(), input.category());
        var pageQuery = new PageQuery(input.page(), input.size());

        PagedResult<StockItem> pagedItems = stockItemRepository.findByStockId(new StockId(stockId), filter,  pageQuery);

        return pagedItems.map(this::toOutput);
    }

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
    @Observed(name = "stock-item.admin.update")
    public StockItemOutput update(Long id, @NonNull UpdateStockItemInput input) {
        StockItem updated = findItemOrThrow(id).update(
                input.name(),
                input.category(),
                input.minimumStock(),
                input.unitCost(),
                input.active()
        );
        var saved = stockItemRepository.save(updated);
        log.info("stock item updated. stockItemId={}", saved.getId().value());
        return toOutput(saved);
    }

    @Transactional
    @Observed(name = "stock-item.admin.delete")
    public void delete(Long id) {
        StockItem item = findItemOrThrow(id);
        productCompositionRepository.deleteByStockItemId(item.getId());
        stockItemRepository.save(item.markDeleted());
        log.info("stock item deleted. stockItemId={}", item.getId().value());
    }

    @Transactional
    @Observed(name = "stock-item.admin.movement")
    public StockItemOutput registerMovement(Long stockItemId, @NonNull RegisterMovementInput input) {
        var updated = switch (input.type()) {
            case ENTRADA -> addStock(findItemForUpdateOrThrow(stockItemId), input.quantity(), input.reason(), requireExpiry(input));
            case SAIDA, PERDA -> deductFromItem(findActiveItemForUpdateOrThrow(stockItemId), input.type(), input.quantity(), input.reason());
            case AJUSTE -> applyAdjustment(findItemForUpdateOrThrow(stockItemId), input.quantity(), input.reason());
        };
        log.info("movement registered. stockItemId={}, movementType={}", updated.id(), input.type());
        return updated;
    }

    @Transactional(readOnly = true)
    public int getBalance(Long stockItemId) {
        StockItem item = findActiveItemForUpdateOrThrow(stockItemId);
        return item.getCurrentQuantity();
    }

    @Transactional
    public void deductStock(Long stockItemId, int quantity, String reason) {
        var updated = deductFromItem(findActiveItemForUpdateOrThrow(stockItemId), MovementType.SAIDA, quantity, reason);
        log.info("stock deducted automatically. stockItemId={}, deductedQuantity={}, newBalance={}, reason={}",
                stockItemId, quantity, updated.currentQuantity(), reason);
    }

    @Transactional
    public void addStock(Long stockItemId, int quantity, String reason) {
        var updated = addStock(
                findItemForUpdateOrThrow(stockItemId),
                quantity,
                reason,
                today().plusDays(reversalLotDays)
        );
        log.info("stock restored automatically. stockItemId={}, addedQuantity={}, newBalance={}, reason={}",
                stockItemId, quantity, updated.currentQuantity(), reason);
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

    private void consumeLots(@NonNull StockItem item, int quantity, MovementType movementType, String reason) {
        List<StockLot> lots = stockLotRepository.findByStockItemIdForUpdate(item.getId());
        List<LotAllocation> allocations = lotAllocationStrategy.allocate(lots, quantity);
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
