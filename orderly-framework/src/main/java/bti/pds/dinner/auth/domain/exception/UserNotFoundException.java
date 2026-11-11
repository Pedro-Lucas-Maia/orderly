package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) {
        super("User not found", message);
    }
}
