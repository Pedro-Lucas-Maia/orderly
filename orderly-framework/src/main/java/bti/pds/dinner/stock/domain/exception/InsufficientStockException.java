package bti.pds.dinner.stock.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InsufficientStockException extends BusinessRuleException {
    public InsufficientStockException(String message) {
        super("Insufficient stock", message, ErrorCategory.CONFLICT);
    }
}
