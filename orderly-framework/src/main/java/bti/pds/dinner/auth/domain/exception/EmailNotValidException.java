package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class EmailNotValidException extends AuthException {
    public EmailNotValidException(String message) {
        super("E-mail not valid", message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
