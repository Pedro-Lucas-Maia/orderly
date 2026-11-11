package bti.pds.dinner.common.exception;

public abstract class DomainValidationException extends DomainException {
    protected DomainValidationException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.VALIDATION);
    }

    protected DomainValidationException(String errorTitle, String message, ErrorCategory category) {
        super(errorTitle, message, category);
    }
}
