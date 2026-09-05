package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class LoginNotValidException extends AuthException {
    public LoginNotValidException(String message) {
        super("Login not valid", message, HttpStatus.UNAUTHORIZED);
    }
}
