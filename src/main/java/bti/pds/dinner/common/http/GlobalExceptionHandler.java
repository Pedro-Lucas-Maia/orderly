package bti.pds.dinner.common.http;

import bti.pds.dinner.common.exception.ForbiddenAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice()
public class GlobalExceptionHandler {
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenAccessException exception,  HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,  HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, exception, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception,  HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, Exception exception, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
