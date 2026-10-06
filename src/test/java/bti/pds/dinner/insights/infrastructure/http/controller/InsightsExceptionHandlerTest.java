package bti.pds.dinner.insights.infrastructure.http.controller;

import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsightsExceptionHandlerTest {

    private final InsightsExceptionHandler handler = new InsightsExceptionHandler();

    @Test
    void mapsMissingStoreToNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/stores/999/insights");

        var response = handler.handleStoreNotFound(new StoreNotFoundException(999L), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Store not found: 999", response.getBody().getMessage());
    }

    @Test
    void mapsInvalidDateToBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/stores/1/insights");
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                LocalDate.class,
                "date",
                null,
                new IllegalArgumentException("invalid")
        );

        var response = handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid date. Use ISO format YYYY-MM-DD.", response.getBody().getMessage());
    }
}
