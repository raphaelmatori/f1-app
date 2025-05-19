package com.f1.app.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_ReturnsProperResponse() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "Not found");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleApiException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    void handleRestClientException_ReturnsInternalServerError() {
        RestClientException ex = new RestClientException("API error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleRestClientException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("API error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Something went wrong"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }
} 