package bti.pds.dinner.shoppingCart.infrastructure.http.response;

import bti.pds.dinner.shoppingCart.application.output.CartOutput;
import bti.pds.dinner.shoppingCart.domain.CartItem;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String id,
        List<CartItem> items,
        BigDecimal subTotal
) {
    public static CartResponse from(@NonNull CartOutput output) {
        return new CartResponse(
                output.id(),
                output.items(),
                output.subTotal()
        );
    }
}
