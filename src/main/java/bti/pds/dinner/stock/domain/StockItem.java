package bti.pds.dinner.stock.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import bti.pds.dinner.stock.domain.exception.InsufficientStockException;

@Getter
@AllArgsConstructor
@Builder
public class StockItem {
    private StockItemId id;
    private StockId stockId;
    private String name;
    private String category;
    private String unit;
    private int currentQuantity;
    private int minimumStock;
    private BigDecimal unitCost;
    private boolean active;
    private LocalDateTime deletedAt;

    public StockItem(
            StockId stockId,
            String name,
            String category,
            String unit,
            int currentQuantity,
            int minimumStock,
            BigDecimal unitCost,
            boolean active
    ) {
        this.id = null;
        this.stockId = stockId;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.currentQuantity = currentQuantity;
        this.minimumStock = minimumStock;
        this.unitCost = unitCost;
        this.active = active;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public StockItem update(String name, String category, Integer minimumStock, BigDecimal unitCost, Boolean active) {
        return StockItem.builder()
                .id(this.id)
                .stockId(this.stockId)
                .name(name != null ? name : this.name)
                .category(category != null ? category : this.category)
                .unit(this.unit)
                .currentQuantity(this.currentQuantity)
                .minimumStock(minimumStock != null ? minimumStock : this.minimumStock)
                .unitCost(unitCost != null ? unitCost : this.unitCost)
                .active(active != null ? active : this.active)
                .deletedAt(this.deletedAt)
                .build();
    }

    public StockItem markDeleted() {
        return StockItem.builder()
                .id(this.id)
                .stockId(this.stockId)
                .name(this.name)
                .category(this.category)
                .unit(this.unit)
                .currentQuantity(this.currentQuantity)
                .minimumStock(this.minimumStock)
                .unitCost(this.unitCost)
                .active(false)
                .deletedAt(LocalDateTime.now())
                .build();
    }

    public StockItem applyMovement(MovementType movementType, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        int newQuantity = switch (movementType) {
            case ENTRADA -> this.currentQuantity + quantity;
            case SAIDA, PERDA -> {
                if (this.currentQuantity < quantity) {
                    throw new InsufficientStockException("Insufficient stock for item: " + this.name);
                }
                yield this.currentQuantity - quantity;
            }
            case AJUSTE -> quantity;
        };

        return StockItem.builder()
                .id(this.id)
                .stockId(this.stockId)
                .name(this.name)
                .category(this.category)
                .unit(this.unit)
                .currentQuantity(newQuantity)
                .minimumStock(this.minimumStock)
                .unitCost(this.unitCost)
                .active(this.active)
                .deletedAt(this.deletedAt)
                .build();
    }
}
