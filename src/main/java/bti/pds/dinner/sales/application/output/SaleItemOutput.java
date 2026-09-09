package bti.pds.dinner.sales.application.output;

import bti.pds.dinner.sales.domain.SaleItem;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record SaleItemOutput(
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static SaleItemOutput from(@NonNull SaleItem item) {
        return new SaleItemOutput(
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
