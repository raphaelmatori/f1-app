package com.f1.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    
    private final HttpStatus status;
    private final String message;
    
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
    
    public ApiException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
} 