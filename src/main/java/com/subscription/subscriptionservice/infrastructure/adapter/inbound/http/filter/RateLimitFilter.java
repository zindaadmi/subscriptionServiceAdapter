package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter
 * Limits requests per IP address
 */
public class RateLimitFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final ObjectMapper objectMapper;
    private final int maxRequestsPerMinute;
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    public RateLimitFilter(ObjectMapper objectMapper, int maxRequestsPerMinute) {
        this.objectMapper = objectMapper;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        String clientIp = getClientIp(request);
        String path = request.getPath();
        
        // Skip rate limiting for health checks
        if (path.equals("/health") || path.startsWith("/health/")) {
            chain.doFilter(request, response);
            return;
        }
        
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        
        long currentTime = System.currentTimeMillis();
        
        // Reset counter if minute has passed
        if (currentTime - info.getWindowStart() > 60000) {
            info.reset(currentTime);
        }
        
        // Check if limit exceeded
        if (info.getCount().get() >= maxRequestsPerMinute) {
            logger.warn("Rate limit exceeded for IP: {}, path: {}", clientIp, path);
            sendRateLimitResponse(response);
            return;
        }
        
        // Increment counter
        info.getCount().incrementAndGet();
        
        // Continue to next filter
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpRequest request) {
        // Try X-Forwarded-For header first (for proxies)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        // Try X-Real-IP header
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        // Fallback to remote address (if available)
        String remoteAddr = request.getHeader("Remote-Addr");
        return remoteAddr != null ? remoteAddr : "unknown";
    }
    
    private void sendRateLimitResponse(HttpResponse response) {
        try {
            response.setStatusCode(429);
            response.setHeader("Content-Type", "application/json");
            response.setHeader("Retry-After", "60");
            
            ErrorResponse errorResponse = new ErrorResponse(
                429,
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please try again later.",
                ""
            );
            
            response.setBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.error("Error creating rate limit response", e);
            response.setStatusCode(429);
            response.setBody("{\"error\":\"Too many requests\"}");
        }
    }
    
    private static class RateLimitInfo {
        private long windowStart;
        private final AtomicInteger count;
        
        public RateLimitInfo() {
            this.windowStart = System.currentTimeMillis();
            this.count = new AtomicInteger(0);
        }
        
        public void reset(long newWindowStart) {
            this.windowStart = newWindowStart;
            this.count.set(0);
        }
        
        public long getWindowStart() {
            return windowStart;
        }
        
        public AtomicInteger getCount() {
            return count;
        }
    }
}

