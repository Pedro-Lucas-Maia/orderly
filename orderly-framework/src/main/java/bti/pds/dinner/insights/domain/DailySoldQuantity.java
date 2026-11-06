package bti.pds.dinner.insights.domain;

import java.time.LocalDate;

public record DailySoldQuantity(
        LocalDate date,
        Long productId,
        int quantity
) {
}
