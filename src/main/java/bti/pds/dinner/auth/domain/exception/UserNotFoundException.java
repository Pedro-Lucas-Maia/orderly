package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String message) {
        super("User not found", message, HttpStatus.NOT_FOUND);
    }
}
