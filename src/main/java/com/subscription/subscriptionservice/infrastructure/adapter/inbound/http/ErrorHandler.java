package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.domain.exception.ApiException;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.ErrorResponse;
import com.framework.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global error handler for HTTP responses
 */
public class ErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private final ObjectMapper objectMapper;
    
    public ErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public HttpResponse handleException(Exception e, String path) {
        try {
            if (e instanceof ApiException) {
                ApiException apiException = (ApiException) e;
                ErrorResponse errorResponse = new ErrorResponse(
                    apiException.getStatusCode(),
                    apiException.getErrorCode(),
                    apiException.getMessage(),
                    path
                );
                
                logger.warn("API Exception: {} - {}", apiException.getErrorCode(), apiException.getMessage());
                
                return new HttpResponse(
                    apiException.getStatusCode(),
                    objectMapper.writeValueAsString(errorResponse)
                );
            } else {
                // Generic exception - don't expose internal details
                logger.error("Unexpected error: {}", e.getMessage(), e);
                
                ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "INTERNAL_ERROR",
                    "An unexpected error occurred",
                    path
                );
                
                return new HttpResponse(
                    500,
                    objectMapper.writeValueAsString(errorResponse)
                );
            }
        } catch (Exception ex) {
            logger.error("Error serializing error response", ex);
            return HttpResponse.internalServerError("{\"error\":\"Internal server error\"}");
        }
    }
}

