package bti.pds.dinner.insights.domain;

import java.time.LocalDate;

public record StockLotSnapshot(
        Long lotId,
        Long stockItemId,
        int quantity,
        LocalDate expiresAt
) {
}
