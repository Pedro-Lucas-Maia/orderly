package bti.pds.dinner.shoppingCart.application.input;

public record UpdateItemQuantityInput(
        String id,
        String productId,
        int quantity
) {
}
