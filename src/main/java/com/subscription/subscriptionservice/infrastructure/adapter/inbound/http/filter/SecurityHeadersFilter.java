package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Headers Filter - Adds security headers to all responses
 */
public class SecurityHeadersFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityHeadersFilter.class);
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // XSS Protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // HSTS (only for HTTPS in production)
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy (adjust based on your needs)
        // response.setHeader("Content-Security-Policy", "default-src 'self'");
        
        // Continue to next filter
        chain.doFilter(request, response);
    }
}

