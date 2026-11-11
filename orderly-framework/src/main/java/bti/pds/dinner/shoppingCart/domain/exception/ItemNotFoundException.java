package bti.pds.dinner.shoppingCart.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class ItemNotFoundException extends ResourceNotFoundException {
    public ItemNotFoundException(String message) {
        super("Cart item not found", message);
    }
}
