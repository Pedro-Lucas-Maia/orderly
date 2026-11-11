package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.UnauthorizedException;

public class TokenNotValidException extends UnauthorizedException {
    public TokenNotValidException(String message) {
        super("Token not valid", message);
    }
}
