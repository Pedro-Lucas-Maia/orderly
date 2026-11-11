package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.ExternalServiceException;

public class ImageStorageException extends ExternalServiceException {
    public ImageStorageException(String message, Throwable cause) {
        super("Image storage error", message, cause);
    }
}
