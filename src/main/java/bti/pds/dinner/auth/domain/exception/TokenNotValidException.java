package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class TokenNotValidException extends AuthException {
    public TokenNotValidException(String message) {
        super("Token not valid", message, HttpStatus.UNAUTHORIZED);
    }
}
