package bti.pds.dinner.shoppingCart.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;

public class EmptyCartException extends BusinessRuleException {
    public EmptyCartException(String message) {
        super("Empty cart", message);
    }
}
