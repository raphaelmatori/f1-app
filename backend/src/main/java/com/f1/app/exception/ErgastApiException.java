package com.f1.app.exception;

public class ErgastApiException extends RuntimeException {
    private final int statusCode;

    public ErgastApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ErgastApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static class RateLimitExceededException extends ErgastApiException {
        public RateLimitExceededException(String message) {
            super(message, 429);
        }
    }

    public static class ResourceNotFoundException extends ErgastApiException {
        public ResourceNotFoundException(String message) {
            super(message, 404);
        }
    }

    public static class ServerErrorException extends ErgastApiException {
        public ServerErrorException(String message) {
            super(message, 500);
        }
    }
} 