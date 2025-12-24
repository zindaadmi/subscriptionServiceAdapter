package com.subscription.subscriptionservice.domain.exception;

/**
 * Exception for 403 Forbidden errors (authorization failures)
 */
public class ForbiddenException extends ApiException {
    
    public ForbiddenException(String message) {
        super(403, "FORBIDDEN", message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(403, "FORBIDDEN", message, cause);
    }
}

