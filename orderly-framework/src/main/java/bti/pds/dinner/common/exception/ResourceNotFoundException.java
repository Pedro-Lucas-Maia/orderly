package bti.pds.dinner.common.exception;

public abstract class ResourceNotFoundException extends DomainException {
    protected ResourceNotFoundException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.RESOURCE_NOT_FOUND);
    }
}
