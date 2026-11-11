package bti.pds.dinner.common.exception;

public abstract class BusinessRuleException extends DomainException {
    protected BusinessRuleException(String errorTitle, String message) {
        super(errorTitle, message, ErrorCategory.VALIDATION);
    }

    protected BusinessRuleException(String errorTitle, String message, ErrorCategory category) {
        super(errorTitle, message, category);
    }
}
