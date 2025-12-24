package com.subscription.subscriptionservice.domain.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(400, "VALIDATION_ERROR", message);
    }
    
    public ValidationException(String field, String message) {
        super(400, "VALIDATION_ERROR", field + ": " + message);
    }
}

