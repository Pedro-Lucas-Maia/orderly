package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.UnauthorizedException;

public class UserNotVerifiedException extends UnauthorizedException {
    public UserNotVerifiedException(String message) {
        super("User not verified", message);
    }
}
