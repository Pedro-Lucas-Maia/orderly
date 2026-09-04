package bti.pds.dinner.inventory.infrastructure.persistence.entity;

import bti.pds.dinner.inventory.domain.StockId;
import bti.pds.dinner.inventory.domain.StockItem;
import bti.pds.dinner.inventory.domain.StockItemId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id")
    private Long stockId;

    private String name;

    private String category;

    private String unit;

    @Column(name = "current_quantity")
    private int currentQuantity;

    @Column(name = "minimum_stock")
    private int minimumStock;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    private boolean active;

    public static StockItemEntity from(@NonNull StockItem stockItem) {
        return StockItemEntity.builder()
                .id(stockItem.getId() != null ? stockItem.getId().value() : null)
                .stockId(stockItem.getStockId().value())
                .name(stockItem.getName())
                .category(stockItem.getCategory())
                .unit(stockItem.getUnit())
                .currentQuantity(stockItem.getCurrentQuantity())
                .minimumStock(stockItem.getMinimumStock())
                .unitCost(stockItem.getUnitCost())
                .active(stockItem.isActive())
                .build();
    }

    public static StockItem toDomain(@NonNull StockItemEntity entity) {
        return StockItem.builder()
                .id(new StockItemId(entity.getId()))
                .stockId(new StockId(entity.getStockId()))
                .name(entity.getName())
                .category(entity.getCategory())
                .unit(entity.getUnit())
                .currentQuantity(entity.getCurrentQuantity())
                .minimumStock(entity.getMinimumStock())
                .unitCost(entity.getUnitCost())
                .active(entity.isActive())
                .build();
    }
}
