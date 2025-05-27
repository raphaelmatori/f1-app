package com.f1.app.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.f1.app.model.ApiError;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        ResponseEntity<ApiError> response = exceptionHandler.handleServiceException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test error message", response.getBody().getMessage());
        assertEquals("TEST_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void handleHttpClientError_ShouldReturnCorrectErrorResponse() {
        // Arrange
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            null,
            null,
            null
        );

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleHttpClientError(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getMessage());
        assertEquals("CLIENT_ERROR_400", response.getBody().getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void handleHttpServerError_ShouldReturnCorrectErrorResponse() {
        // Arrange
        HttpServerErrorException exception = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            null,
            null,
            null
        );

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleHttpServerError(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getMessage());
        assertEquals("SERVER_ERROR_500", response.getBody().getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }

    @Test
    void handleResourceAccessException_ShouldReturnCorrectErrorResponse() {
        // Arrange
        ResourceAccessException exception = new ResourceAccessException("Connection refused");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleResourceAccessException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External service is unavailable", response.getBody().getMessage());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getBody().getStatus());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnCorrectErrorResponse() {
        // Arrange
        when(methodArgumentTypeMismatchException.getName()).thenReturn("year");
        when(methodArgumentTypeMismatchException.getValue()).thenReturn("abc");
        when(methodArgumentTypeMismatchException.getRequiredType()).thenReturn((Class)Integer.class);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleMethodArgumentTypeMismatch(
            methodArgumentTypeMismatchException,
            request
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The parameter 'year' of value 'abc' could not be converted to type 'Integer'", 
            response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void handleRestClientException_ShouldReturnServiceUnavailable() {
        // Arrange
        RestClientException exception = new RestClientException("API error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleRestClientException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External API service error", response.getBody().getMessage());
        assertEquals("EXTERNAL_API_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getBody().getStatus());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleGenericException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }
} 
