package com.subscription.subscriptionservice.domain.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(String message) {
        super(404, "USER_NOT_FOUND", message);
    }
}

