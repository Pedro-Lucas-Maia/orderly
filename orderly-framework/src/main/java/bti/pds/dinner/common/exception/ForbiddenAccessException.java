package bti.pds.dinner.common.exception;

public class ForbiddenAccessException extends DomainException {
    public ForbiddenAccessException(String message) {
        super("Forbidden", message, ErrorCategory.FORBIDDEN);
    }
}
