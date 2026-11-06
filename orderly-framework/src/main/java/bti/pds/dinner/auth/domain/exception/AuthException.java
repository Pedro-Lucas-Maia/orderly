package bti.pds.dinner.auth.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AuthException extends RuntimeException {
    private final String error;
    private final HttpStatus status;

    protected AuthException(String error, String message, HttpStatus status) {
        super(message);
        this.error = error;
        this.status = status;
    }

}
