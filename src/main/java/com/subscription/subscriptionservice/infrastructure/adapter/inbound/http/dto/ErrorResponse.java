package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int statusCode;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    public ErrorResponse(int statusCode, String errorCode, String message, String path) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}

