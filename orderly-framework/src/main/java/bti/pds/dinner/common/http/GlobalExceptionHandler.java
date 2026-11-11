package bti.pds.dinner.common.http;

import bti.pds.dinner.common.exception.DomainException;
import bti.pds.dinner.common.exception.ErrorCategory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Captura TODAS as exceções de negócio de TODOS os módulos (auth, sales, product, stock, store, cart, etc.)
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            @NonNull DomainException ex,
            @NonNull HttpServletRequest request
    ) {
        HttpStatus status = mapCategoryToStatus(ex.getCategory());
        return buildAndLogResponse(status, ex.getErrorTitle(), ex.getMessage(), request.getRequestURI(), ex);
    }

    // Captura validações de entidades de domínio que ainda lançam IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            @NonNull IllegalArgumentException ex,
            @NonNull HttpServletRequest request
    ) {
        return buildAndLogResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // Captura erros de @Valid nos Requests (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpServletRequest request
    ) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildAndLogResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation Error",
                errorMessage,
                request.getRequestURI(),
                ex
        );
    }

    // Captura parâmetros inválidos na URL/Query (ex: texto onde se esperava UUID ou data)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            @NonNull MethodArgumentTypeMismatchException ex,
            @NonNull HttpServletRequest request
    ) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        if (ex.getRequiredType() == LocalDate.class) {
            message = "Invalid date. Use ISO format YYYY-MM-DD.";
        }
        return buildAndLogResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter",
                message,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpServletRequest request
    ) {
        return buildAndLogResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter",
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // Fallback de Segurança: Captura qualquer bug inesperado (NullPointer, falha de banco, etc.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            @NonNull Exception ex,
            @NonNull HttpServletRequest request
    ) {
        // Exceções nativas do Spring MVC (405, 404, 415, ...) já carregam o status correto
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatusCode statusCode = springError.getStatusCode();
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            String title = status != null ? status.getReasonPhrase() : "Request error";
            return buildAndLogResponse(statusCode, title, ex.getMessage(), request.getRequestURI(), ex);
        }
        return buildAndLogResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred", // Não vaza detalhes de SQL/infra para o cliente
                request.getRequestURI(),
                ex
        );
    }

    private ResponseEntity<ErrorResponse> buildAndLogResponse(
            HttpStatusCode status,
            String errorTitle,
            String message,
            String path,
            Exception ex
    ) {
        // 5xx = ERROR com StackTrace | 4xx = WARN sem StackTrace
        if (status.is5xxServerError()) {
            log.error("Server error occurred. status={}, error={}, path={}, message={}",
                    status.value(), errorTitle, path, ex.getMessage(), ex);
        } else {
            log.warn("Client error occurred. status={}, error={}, path={}, message={}",
                    status.value(), errorTitle, path, message);
        }

        ErrorResponse body = ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(status.value())
                .error(errorTitle)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus mapCategoryToStatus(ErrorCategory category) {
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case EXTERNAL_FAILURE, INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
