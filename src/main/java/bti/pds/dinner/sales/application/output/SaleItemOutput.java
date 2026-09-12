package bti.pds.dinner.sales.application.output;

import java.math.BigDecimal;

public record SaleItemOutput(
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
