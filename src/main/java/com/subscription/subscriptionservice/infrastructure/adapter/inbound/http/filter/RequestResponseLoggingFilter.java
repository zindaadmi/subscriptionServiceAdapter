package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Request/Response Logging Filter
 * Logs all incoming requests and responses with request ID for tracing
 */
public class RequestResponseLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOGGER");
    
    private final boolean logRequestBody;
    private final boolean logResponseBody;
    private final int maxBodyLength;
    
    public RequestResponseLoggingFilter() {
        this.logRequestBody = true;
        this.logResponseBody = false; // Don't log response bodies by default (can be large)
        this.maxBodyLength = 1000; // Max length to log
    }
    
    public RequestResponseLoggingFilter(boolean logRequestBody, boolean logResponseBody, int maxBodyLength) {
        this.logRequestBody = logRequestBody;
        this.logResponseBody = logResponseBody;
        this.maxBodyLength = maxBodyLength;
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getPath());
        
        // Add request ID to response headers
        response.setHeader("X-Request-ID", requestId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request
            logRequest(request, requestId);
            
            // Continue filter chain
            chain.doFilter(request, response);
            
            // Calculate response time
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log response
            logResponse(request, response, requestId, responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logError(request, response, requestId, responseTime, e);
            throw e;
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }
    
    private void logRequest(HttpRequest request, String requestId) {
        StringBuilder log = new StringBuilder();
        log.append("REQUEST [").append(requestId).append("] ");
        log.append(request.getMethod()).append(" ").append(request.getPath());
        
        // Log query parameters if present
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            log.append("?").append(request.getQueryParams());
        }
        
        // Log client IP
        String clientIp = getClientIp(request);
        if (clientIp != null) {
            log.append(" from ").append(clientIp);
        }
        
        // Log user if authenticated
        String username = request.getHeader("X-Username");
        if (username != null) {
            log.append(" user=").append(username);
        }
        
        requestLogger.info(log.toString());
        
        // Log request body if enabled and present
        if (logRequestBody && request.getBody() != null && !request.getBody().isEmpty()) {
            String body = request.getBody();
            if (body.length() > maxBodyLength) {
                body = body.substring(0, maxBodyLength) + "... (truncated)";
            }
            requestLogger.debug("REQUEST BODY [{}]: {}", requestId, body);
        }
    }
    
    private void logResponse(HttpRequest request, HttpResponse response, String requestId, long responseTime) {
        StringBuilder log = new StringBuilder();
        log.append("RESPONSE [").append(requestId).append("] ");
        log.append(request.getMethod()).append(" ").append(request.getPath());
        log.append(" ").append(response.getStatusCode());
        log.append(" (").append(responseTime).append("ms)");
        
        requestLogger.info(log.toString());
        
        // Log response body if enabled and present
        if (logResponseBody && response.getBody() != null && !response.getBody().isEmpty()) {
            String body = response.getBody();
            if (body.length() > maxBodyLength) {
                body = body.substring(0, maxBodyLength) + "... (truncated)";
            }
            requestLogger.debug("RESPONSE BODY [{}]: {}", requestId, body);
        }
    }
    
    private void logError(HttpRequest request, HttpResponse response, String requestId, long responseTime, Exception e) {
        StringBuilder log = new StringBuilder();
        log.append("ERROR [").append(requestId).append("] ");
        log.append(request.getMethod()).append(" ").append(request.getPath());
        log.append(" ").append(response.getStatusCode() != 0 ? response.getStatusCode() : 500);
        log.append(" (").append(responseTime).append("ms)");
        log.append(" error=").append(e.getClass().getSimpleName());
        
        requestLogger.error(log.toString(), e);
    }
    
    private String getClientIp(HttpRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        return request.getHeader("Remote-Addr");
    }
}

