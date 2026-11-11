package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.DomainValidationException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class InvalidPasswordException extends DomainValidationException {
    public InvalidPasswordException(String message) {
        super("Invalid password", message, ErrorCategory.UNPROCESSABLE);
    }
}
