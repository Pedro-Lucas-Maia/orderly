package bti.pds.dinner.sales.application.input;

import java.util.UUID;

public record CheckoutInput(
        UUID userId,
        String shoppingCartId,
        UUID addressId,
        Long storeId,
        String observation
) {
}
