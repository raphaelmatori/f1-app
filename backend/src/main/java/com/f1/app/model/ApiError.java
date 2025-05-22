package com.f1.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String message;
    private String code;
    private int status;
    private long timestamp;

    public ApiError(String message, String code, int status) {
        this.message = message;
        this.code = code;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
} 