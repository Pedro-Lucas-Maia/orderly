package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class RoleNotFoundException extends ResourceNotFoundException {
    public RoleNotFoundException(String message) {
        super("Role not found", message);
    }
}
