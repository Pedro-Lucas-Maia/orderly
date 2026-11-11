package bti.pds.dinner.common.http;

import bti.pds.dinner.auth.domain.exception.LoginNotValidException;
import bti.pds.dinner.common.exception.ForbiddenAccessException;
import bti.pds.dinner.product.domain.exception.ImageStorageException;
import bti.pds.dinner.stock.domain.exception.InsufficientStockException;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/insights");
        return request;
    }

    @Test
    void mapsMissingStoreToNotFound() {
        var response = handler.handleDomainException(new StoreNotFoundException(), request());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Default Store not found", response.getBody().getMessage());
    }

    @Test
    void mapsConflictingBusinessRuleToConflict() {
        var response = handler.handleDomainException(new InsufficientStockException("no stock"), request());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void mapsUnauthorizedAndForbidden() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleDomainException(new LoginNotValidException("bad"), request()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleDomainException(new ForbiddenAccessException("denied"), request()).getStatusCode());
    }

    @Test
    void mapsExternalFailureToServerErrorPreservingCause() {
        IOException cause = new IOException("timeout");
        ImageStorageException exception = new ImageStorageException("Failed to upload", cause);

        var response = handler.handleDomainException(exception, request());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void mapsIllegalArgumentToBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad value"), request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("bad value", response.getBody().getMessage());
    }

    @Test
    void mapsInvalidDateToBadRequest() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                LocalDate.class,
                "date",
                null,
                new IllegalArgumentException("invalid")
        );

        var response = handler.handleTypeMismatch(exception, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid date. Use ISO format YYYY-MM-DD.", response.getBody().getMessage());
    }

    @Test
    void hidesUnexpectedErrorDetails() {
        var response = handler.handleUnexpectedException(new IllegalStateException("db password leaked"), request());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
