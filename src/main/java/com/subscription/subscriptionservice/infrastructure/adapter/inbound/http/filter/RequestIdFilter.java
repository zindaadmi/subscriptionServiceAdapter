package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Request ID Filter - Adds unique request ID to all requests
 * Must be first in filter chain
 */
public class RequestIdFilter implements Filter {
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        // Check if request ID already exists (from load balancer, etc.)
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Add to MDC for logging
        MDC.put("requestId", requestId);
        
        // Add to response headers
        response.setHeader("X-Request-ID", requestId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

