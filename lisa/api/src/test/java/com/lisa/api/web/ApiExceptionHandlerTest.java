package com.lisa.api.web;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void validationErrorReturnsBadRequest() {
        ResponseEntity<Map<String, String>> response = handler.validationError();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid request"), response.getBody());
    }

    @Test
    void unexpectedErrorReturnsInternalServerError() {
        ResponseEntity<Map<String, String>> response = handler.unexpectedError();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("error", "Internal server error"), response.getBody());
    }
}