package bti.pds.dinner.stock.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
public class StockLot {
    private StockLotId id;
    private StockItemId stockItemId;
    private int quantity;
    private LocalDate expiresAt;
    private LocalDateTime receivedAt;

    public StockLot(StockItemId stockItemId, int quantity, LocalDate expiresAt, LocalDateTime receivedAt) {
        this.id = null;
        this.stockItemId = Objects.requireNonNull(stockItemId, "Stock item is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiry date is required");
        this.receivedAt = Objects.requireNonNull(receivedAt, "Received at is required");
        this.quantity = requirePositiveQuantity(quantity);
    }

    public StockLot consume(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Cannot consume more than the lot quantity");
        }
        return StockLot.builder()
                .id(this.id)
                .stockItemId(this.stockItemId)
                .quantity(this.quantity - amount)
                .expiresAt(this.expiresAt)
                .receivedAt(this.receivedAt)
                .build();
    }

    public boolean isEmpty() {
        return this.quantity == 0;
    }

    private static int requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Lot quantity must be greater than 0");
        }
        return quantity;
    }
}
