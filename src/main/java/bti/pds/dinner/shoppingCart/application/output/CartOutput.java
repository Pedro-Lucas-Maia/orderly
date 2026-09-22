package bti.pds.dinner.shoppingCart.application.output;

import bti.pds.dinner.shoppingCart.domain.CartItem;
import bti.pds.dinner.shoppingCart.domain.ShoppingCart;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.List;

public record CartOutput(
        String id,
        List<CartItem> items,
        BigDecimal subTotal
) {
    public static CartOutput from(@NonNull ShoppingCart cart) {
        return new CartOutput(
                cart.getId().id(),
                cart.getItems(),
                cart.getSubTotal()
        );
    }
}
