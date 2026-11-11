package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class FileUnreadableException extends BusinessRuleException {
    public FileUnreadableException(String message) {
        super("File unreadable", message, ErrorCategory.UNPROCESSABLE);
    }
}
