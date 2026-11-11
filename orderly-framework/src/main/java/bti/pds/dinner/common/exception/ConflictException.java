package bti.pds.dinner.common.exception;

public abstract class ConflictException extends DomainException {
    protected ConflictException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.CONFLICT);
    }
}
