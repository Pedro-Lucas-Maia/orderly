package bti.pds.dinner.auth.domain.exception;

import bti.pds.dinner.common.exception.DataIntegrityException;

public class RoleNotFoundException extends DataIntegrityException {
    public RoleNotFoundException(String message) {
        super("Role not found", message);
    }
}
