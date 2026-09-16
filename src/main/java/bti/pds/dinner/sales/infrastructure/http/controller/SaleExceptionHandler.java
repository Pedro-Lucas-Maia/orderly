package bti.pds.dinner.sales.infrastructure.http.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import bti.pds.dinner.common.http.ErrorResponse;
import bti.pds.dinner.sales.domain.exception.InvalidSaleItemException;
import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import bti.pds.dinner.sales.domain.exception.SaleException;
import bti.pds.dinner.sales.domain.exception.SaleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "bti.pds.dinner.sales")
public class SaleExceptionHandler {

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SaleNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(InvalidSaleStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(InvalidSaleStateException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler({InvalidSaleItemException.class, SaleException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception, request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, RuntimeException exception, HttpServletRequest request) {
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
