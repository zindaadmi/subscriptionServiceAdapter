package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request Size Limit Filter
 * Prevents oversized requests (DoS protection)
 */
public class RequestSizeLimitFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestSizeLimitFilter.class);
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024; // 10MB
    
    private final long maxRequestSize;
    private final ObjectMapper objectMapper;
    
    public RequestSizeLimitFilter() {
        this(DEFAULT_MAX_SIZE);
    }
    
    public RequestSizeLimitFilter(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        String contentLength = request.getHeader("Content-Length");
        
        if (contentLength != null) {
            try {
                long size = Long.parseLong(contentLength);
                if (size > maxRequestSize) {
                    logger.warn("Request size exceeded limit: {} bytes (max: {} bytes) for path: {}", 
                        size, maxRequestSize, request.getPath());
                    
                    response.setStatusCode(413);
                    response.setHeader("Content-Type", "application/json");
                    
                    ErrorResponse errorResponse = new ErrorResponse(
                        413,
                        "PAYLOAD_TOO_LARGE",
                        "Request body exceeds maximum allowed size of " + (maxRequestSize / 1024 / 1024) + "MB",
                        request.getPath()
                    );
                    
                    response.setBody(objectMapper.writeValueAsString(errorResponse));
                    return;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Content-Length header: {}", contentLength);
            }
        }
        
        // Check actual body size if available
        if (request.getBody() != null && request.getBody().length() > maxRequestSize) {
            logger.warn("Request body size exceeded limit: {} bytes (max: {} bytes) for path: {}", 
                request.getBody().length(), maxRequestSize, request.getPath());
            
            response.setStatusCode(413);
            response.setHeader("Content-Type", "application/json");
            
            ErrorResponse errorResponse = new ErrorResponse(
                413,
                "PAYLOAD_TOO_LARGE",
                "Request body exceeds maximum allowed size of " + (maxRequestSize / 1024 / 1024) + "MB",
                request.getPath()
            );
            
            response.setBody(objectMapper.writeValueAsString(errorResponse));
            return;
        }
        
        chain.doFilter(request, response);
    }
}

