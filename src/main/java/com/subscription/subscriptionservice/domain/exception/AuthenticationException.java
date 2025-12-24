package com.subscription.subscriptionservice.domain.exception;

public class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super(401, "AUTHENTICATION_ERROR", message);
    }
}

