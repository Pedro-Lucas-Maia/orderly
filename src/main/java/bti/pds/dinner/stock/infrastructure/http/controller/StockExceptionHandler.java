package bti.pds.dinner.stock.infrastructure.http.controller;

import bti.pds.dinner.common.http.ErrorResponse;
import bti.pds.dinner.stock.domain.exception.InsufficientStockException;
import bti.pds.dinner.stock.domain.exception.MissingLotExpiryException;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import bti.pds.dinner.stock.domain.exception.StockNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "bti.pds.dinner.stock")
public class StockExceptionHandler {

    @ExceptionHandler({StockNotFoundException.class, StockItemNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(MissingLotExpiryException.class)
    public ResponseEntity<ErrorResponse> handleMissingExpiry(MissingLotExpiryException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
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
