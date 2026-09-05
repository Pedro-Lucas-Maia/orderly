package bti.pds.dinner.auth.domain.exception;

import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends AuthException {
    public RoleNotFoundException(String message) {
        super("Role not found", message, HttpStatus.NOT_FOUND);
    }
}
