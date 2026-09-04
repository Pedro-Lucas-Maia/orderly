package bti.pds.dinner.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StockMovement {
    private StockMovementId id;
    private StockItemId stockItemId;
    private MovementType type;
    private int quantity;
    private LocalDateTime occurredAt;
    private String reason;

    public StockMovement(
            StockItemId stockItemId,
            MovementType type,
            int quantity,
            LocalDateTime occurredAt,
            String reason
    ) {
        this.id = null;
        this.stockItemId = stockItemId;
        this.type = type;
        this.quantity = quantity;
        this.occurredAt = occurredAt;
        this.reason = reason;
    }
}
