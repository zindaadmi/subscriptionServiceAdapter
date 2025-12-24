package com.subscription.subscriptionservice.domain.exception;

/**
 * Base exception for all API errors
 */
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;
    
    public ApiException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    public ApiException(int statusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

