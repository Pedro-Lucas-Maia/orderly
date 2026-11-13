package bti.pds.dinner.common.exception;

public abstract class InternalErrorException extends DomainException {
    protected InternalErrorException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.INTERNAL_ERROR);
    }

    protected InternalErrorException(String errorTitle, String message, Throwable cause) {
        super(errorTitle, message, ErrorCategory.INTERNAL_ERROR, cause);
    }
}
