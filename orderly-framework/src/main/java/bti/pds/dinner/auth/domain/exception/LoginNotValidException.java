package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.UnauthorizedException;

public class LoginNotValidException extends UnauthorizedException {
    public LoginNotValidException(String message) {
        super("Login not valid", message);
    }
}
