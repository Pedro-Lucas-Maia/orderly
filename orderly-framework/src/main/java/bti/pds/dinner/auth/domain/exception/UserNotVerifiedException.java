package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class UserNotVerifiedException extends AuthException {
    public UserNotVerifiedException(String message) {
        super("User not verified", message, HttpStatus.UNAUTHORIZED);
    }
}
