package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.UnauthorizedException;

public class UserLockedException extends UnauthorizedException {
    public UserLockedException(String message) {
        super("User locked", message);
    }
}
