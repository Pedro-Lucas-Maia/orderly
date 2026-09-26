package bti.pds.dinner.product.infrastructure.http.controller;

import bti.pds.dinner.common.http.ErrorResponse;
import bti.pds.dinner.product.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.InvalidUrlException;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "bti.pds.dinner.product")
public class ProductExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(ProductCompositionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductCompositionNotFoundException(ProductCompositionNotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(FileUnreadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableFile(FileUnreadableException e, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, e, request);
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ErrorResponse> handleImageException(ImageStorageException e, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException e, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, e, request);
    }

    @ExceptionHandler(UrlNotValidException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotValid(InvalidUrlException e, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, e, request);
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
