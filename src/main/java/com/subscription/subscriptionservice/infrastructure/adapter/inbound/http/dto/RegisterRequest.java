package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto;

import lombok.Data;

/**
 * Request DTO for user registration
 */
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String mobileNumber;
}

