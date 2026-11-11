package bti.pds.dinner.common.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {
    private final String errorTitle;
    private final ErrorCategory category;

    protected DomainException(String errorTitle, String message, ErrorCategory category) {
        super(message);
        this.errorTitle = errorTitle;
        this.category = category;
    }

    protected DomainException(String errorTitle, String message, ErrorCategory category, Throwable cause) {
        super(message, cause);
        this.errorTitle = errorTitle;
        this.category = category;
    }
}
