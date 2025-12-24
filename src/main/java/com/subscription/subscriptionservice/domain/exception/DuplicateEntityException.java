package com.subscription.subscriptionservice.domain.exception;

public class DuplicateEntityException extends ApiException {
    public DuplicateEntityException(String message) {
        super(409, "DUPLICATE_ENTITY", message);
    }
}

