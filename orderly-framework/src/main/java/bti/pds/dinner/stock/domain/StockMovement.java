package bti.pds.dinner.stock.domain;

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
    private StockLotId lotId;

    public StockMovement(
            StockItemId stockItemId,
            MovementType type,
            int quantity,
            LocalDateTime occurredAt,
            String reason
    ) {
        this(stockItemId, type, quantity, occurredAt, reason, null);
    }

    public StockMovement(
            StockItemId stockItemId,
            MovementType type,
            int quantity,
            LocalDateTime occurredAt,
            String reason,
            StockLotId lotId
    ) {
        this.id = null;
        this.stockItemId = stockItemId;
        this.type = type;
        this.quantity = quantity;
        this.occurredAt = occurredAt;
        this.reason = reason;
        this.lotId = lotId;
    }
}
