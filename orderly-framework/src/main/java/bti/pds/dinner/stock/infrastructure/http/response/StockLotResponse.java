package bti.pds.dinner.stock.infrastructure.http.response;

import bti.pds.dinner.stock.application.output.StockLotOutput;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockLotResponse(
        Long lotId,
        Long stockItemId,
        int quantity,
        LocalDate expiresAt,
        LocalDateTime receivedAt,
        long daysUntilExpiry
) {
    public static StockLotResponse from(@NonNull StockLotOutput output) {
        return new StockLotResponse(
                output.lotId(),
                output.stockItemId(),
                output.quantity(),
                output.expiresAt(),
                output.receivedAt(),
                output.daysUntilExpiry()
        );
    }
}
