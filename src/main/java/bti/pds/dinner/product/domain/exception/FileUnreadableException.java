package bti.pds.dinner.product.domain.exception;

public class FileUnreadableException extends RuntimeException {
    public FileUnreadableException(String message) {
        super(message);
    }
}
