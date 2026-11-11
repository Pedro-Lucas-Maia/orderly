package bti.pds.dinner.shoppingCart.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InvalidInputException extends BusinessRuleException {
    public InvalidInputException(String message) {
        super("Invalid cart input", message, ErrorCategory.UNPROCESSABLE);
    }
}
