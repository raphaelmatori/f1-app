package com.f1.app.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionTest {
    @Test
    void constructorAndGetters_WorkAsExpected() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "Bad request error");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Bad request error", ex.getMessage());
        assertEquals("Bad request error", ex.getMessage());
    }
} 
