package bti.pds.dinner.sales.infrastructure.http.response;

import bti.pds.dinner.sales.application.output.SaleItemOutput;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record SaleItemResponse(
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
)
{
    public static SaleItemResponse from(@NonNull SaleItemOutput output) {
        return new SaleItemResponse(
                output.productId(),
                output.quantity(),
                output.unitPrice(),
                output.subtotal()
        );
    }
}
