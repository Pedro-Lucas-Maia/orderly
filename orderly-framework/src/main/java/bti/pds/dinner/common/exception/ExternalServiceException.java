package bti.pds.dinner.common.exception;

public abstract class ExternalServiceException extends DomainException {
    protected ExternalServiceException(String errorTitle, String message, Throwable cause) {
        super(errorTitle, message, ErrorCategory.EXTERNAL_FAILURE, cause);
    }
}
