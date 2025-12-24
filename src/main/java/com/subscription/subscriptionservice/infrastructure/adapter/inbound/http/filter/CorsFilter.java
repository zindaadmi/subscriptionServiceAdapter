package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORS Filter - Handles Cross-Origin Resource Sharing
 */
public class CorsFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);
    
    private final String allowedOrigins;
    private final boolean allowCredentials;
    
    public CorsFilter() {
        this.allowedOrigins = "*"; // Configure via application.yml in production
        this.allowCredentials = false;
    }
    
    public CorsFilter(String allowedOrigins, boolean allowCredentials) {
        this.allowedOrigins = allowedOrigins;
        this.allowCredentials = allowCredentials;
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        String origin = request.getHeader("Origin");
        
        // Set CORS headers
        if (allowedOrigins.equals("*") || (origin != null && allowedOrigins.contains(origin))) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigins.equals("*") ? "*" : origin);
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,X-API-Key,X-Username");
            response.setHeader("Access-Control-Max-Age", "3600");
            
            if (allowCredentials) {
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
        }
        
        // Handle preflight requests
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatusCode(200);
            return;
        }
        
        // Continue to next filter
        chain.doFilter(request, response);
    }
}

