package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.DomainValidationException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class EmailNotValidException extends DomainValidationException {
    public EmailNotValidException(String message) {
        super("E-mail not valid", message, ErrorCategory.UNPROCESSABLE);
    }
}
