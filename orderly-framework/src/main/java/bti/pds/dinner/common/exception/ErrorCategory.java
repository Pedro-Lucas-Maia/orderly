package bti.pds.dinner.common.exception;

public enum ErrorCategory {
    VALIDATION,          // 400 Bad Request
    UNAUTHORIZED,        // 401 Unauthorized
    FORBIDDEN,           // 403 Forbidden
    RESOURCE_NOT_FOUND,  // 404 Not Found
    CONFLICT,            // 409 Conflict
    UNPROCESSABLE,       // 422 Unprocessable Entity
    EXTERNAL_FAILURE,    // 500 Falha em integração (Cloudinary, Resend, etc.)
    INTERNAL_ERROR       // 500 Erro de configuração interna
}
