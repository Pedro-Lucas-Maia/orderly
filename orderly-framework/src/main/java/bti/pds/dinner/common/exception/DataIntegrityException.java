package bti.pds.dinner.common.exception;

public class DataIntegrityException extends InternalErrorException {
    public DataIntegrityException(String message) {
        super("Data Integrity Error", message);
    }

    public DataIntegrityException(String errorTitle, String message) {
        super(errorTitle, message);
    }

    public DataIntegrityException(String errorTitle, String message, Throwable cause) {
        super(errorTitle, message, cause);
    }
}
