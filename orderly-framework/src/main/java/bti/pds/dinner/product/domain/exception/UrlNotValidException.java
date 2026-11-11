package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class UrlNotValidException extends BusinessRuleException {
    public UrlNotValidException(String message) {
        super("Invalid URL", message, ErrorCategory.UNPROCESSABLE);
    }
}
