package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InvalidInputException extends BusinessRuleException {
    public InvalidInputException(String message) {
        super("Invalid product input", message, ErrorCategory.UNPROCESSABLE);
    }
}
