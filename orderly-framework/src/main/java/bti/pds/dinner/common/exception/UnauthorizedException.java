package bti.pds.dinner.common.exception;

public abstract class UnauthorizedException extends DomainException {
    protected UnauthorizedException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.UNAUTHORIZED);
    }
}
