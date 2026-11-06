package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class UserLockedException extends AuthException {
    public UserLockedException(String message) {
        super("User locked", message, HttpStatus.UNAUTHORIZED);
    }
}
