package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidProfileUpdateException extends AuthException {
    public InvalidProfileUpdateException(String message) {
        super("Invalid profile update", message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
