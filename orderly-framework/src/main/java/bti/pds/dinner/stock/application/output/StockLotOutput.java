package bti.pds.dinner.stock.application.output;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockLotOutput(
        Long lotId,
        Long stockItemId,
        int quantity,
        LocalDate expiresAt,
        LocalDateTime receivedAt,
        long daysUntilExpiry
) {
}
