package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException(String message) {
        super("Invalid password", message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
