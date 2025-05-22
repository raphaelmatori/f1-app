package com.f1.app.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final String code;
    private final int status;

    public ServiceException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public ServiceException(String message, String code, int status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }
} 