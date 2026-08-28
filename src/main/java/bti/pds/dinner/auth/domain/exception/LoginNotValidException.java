package bti.pds.dinner.auth.domain.exception;

public class LoginNotValidException extends RuntimeException {
    public LoginNotValidException(String message) {
        super(message);
    }
}
