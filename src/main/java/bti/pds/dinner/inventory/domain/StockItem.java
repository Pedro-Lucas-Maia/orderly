package bti.pds.dinner.inventory.domain;

import bti.pds.dinner.inventory.domain.exception.InsufficientStockException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

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
                .build();
    }
}