package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class CpfNotValidException extends AuthException {
    public CpfNotValidException(String message) {
        super("CPF not valid", message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
