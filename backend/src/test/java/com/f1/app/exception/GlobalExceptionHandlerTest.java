package com.f1.app.exception;

import com.f1.app.model.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleServiceException_ShouldReturnCorrectErrorResponse() {
        // Arrange
        ServiceException exception = new ServiceException(
                "Test error message",
                "TEST_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleServiceException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test error message", response.getBody().getMessage());
        assertEquals("TEST_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleRestClientException_ShouldReturnServiceUnavailable() {
        // Arrange
        RestClientException exception = new RestClientException("API error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleRestClientException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External API service error", response.getBody().getMessage());
        assertEquals("EXTERNAL_API_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    void handleServiceException_WithCause_ShouldReturnCorrectErrorResponse() {
        // Arrange
        Throwable cause = new RuntimeException("Original error");
        ServiceException exception = new ServiceException(
                "Test error message",
                "TEST_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                cause
        );

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleServiceException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test error message", response.getBody().getMessage());
        assertEquals("TEST_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getTimestamp() > 0);
    }
} 
