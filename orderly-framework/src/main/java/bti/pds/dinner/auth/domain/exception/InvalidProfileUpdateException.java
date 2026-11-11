package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.DomainValidationException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InvalidProfileUpdateException extends DomainValidationException {
    public InvalidProfileUpdateException(String message) {
        super("Invalid profile update", message, ErrorCategory.UNPROCESSABLE);
    }
}
