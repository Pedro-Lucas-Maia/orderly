package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.ExternalServiceException;

public class EmailDeliveryException extends ExternalServiceException {
    public EmailDeliveryException(String message, Throwable cause) {
        super("Email not sent", message, cause);
    }
}
