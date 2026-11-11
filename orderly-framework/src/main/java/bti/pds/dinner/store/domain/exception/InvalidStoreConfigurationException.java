package bti.pds.dinner.store.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InvalidStoreConfigurationException extends BusinessRuleException {
    public InvalidStoreConfigurationException(String message) {
        super("Invalid store configuration", message, ErrorCategory.UNPROCESSABLE);
    }
}
