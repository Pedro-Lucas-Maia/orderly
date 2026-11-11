package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.DomainValidationException;
import bti.pds.dinner.common.exception.ErrorCategory;

public class CpfNotValidException extends DomainValidationException {
    public CpfNotValidException(String message) {
        super("CPF not valid", message, ErrorCategory.UNPROCESSABLE);
    }
}
