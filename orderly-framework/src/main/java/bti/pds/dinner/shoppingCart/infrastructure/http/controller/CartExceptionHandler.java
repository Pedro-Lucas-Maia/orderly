package bti.pds.dinner.shoppingCart.infrastructure.http.controller;

import bti.pds.dinner.common.http.ErrorResponse;
import bti.pds.dinner.shoppingCart.domain.exceptions.EmptyCartException;
import bti.pds.dinner.shoppingCart.domain.exceptions.InvalidInputException;
import bti.pds.dinner.shoppingCart.domain.exceptions.ItemNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "bti.pds.dinner.shoppingCart")
public class CartExceptionHandler {
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCartException(EmptyCartException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInputException(InvalidInputException e, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, e, request);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(ItemNotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, e, request);
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
