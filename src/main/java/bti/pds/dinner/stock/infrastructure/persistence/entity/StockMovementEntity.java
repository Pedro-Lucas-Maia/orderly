package bti.pds.dinner.stock.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.domain.MovementType;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockMovement;
import bti.pds.dinner.stock.domain.StockMovementId;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_item_id")
    private Long stockItemId;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private int quantity;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    private String reason;

    public static StockMovementEntity from(@NonNull StockMovement stockMovement) {
        return StockMovementEntity.builder()
                .id(stockMovement.getId() != null ? stockMovement.getId().value() : null)
                .stockItemId(stockMovement.getStockItemId().value())
                .type(stockMovement.getType())
                .quantity(stockMovement.getQuantity())
                .occurredAt(stockMovement.getOccurredAt())
                .reason(stockMovement.getReason())
                .build();
    }

    public static StockMovement toDomain(@NonNull StockMovementEntity entity) {
        return StockMovement.builder()
                .id(new StockMovementId(entity.getId()))
                .stockItemId(new StockItemId(entity.getStockItemId()))
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .occurredAt(entity.getOccurredAt())
                .reason(entity.getReason())
                .build();
    }
}
