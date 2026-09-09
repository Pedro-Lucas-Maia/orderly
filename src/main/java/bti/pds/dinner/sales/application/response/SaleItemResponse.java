package bti.pds.dinner.sales.application.response;

import java.math.BigDecimal;

public record SaleItemResponse(
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
