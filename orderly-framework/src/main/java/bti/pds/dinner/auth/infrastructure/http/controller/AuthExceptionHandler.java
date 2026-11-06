package bti.pds.dinner.auth.infrastructure.http.controller;

import bti.pds.dinner.auth.domain.exception.AuthException;
import bti.pds.dinner.common.http.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "bti.pds.dinner.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(@NonNull AuthException e, @NonNull HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(e.getStatus().value())
                .error(e.getError())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }
}
