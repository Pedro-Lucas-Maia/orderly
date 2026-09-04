package bti.pds.dinner.inventory.domain;

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
}
