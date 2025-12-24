package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance Monitoring Filter
 * Tracks slow requests and performance metrics
 */
public class PerformanceMonitoringFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringFilter.class);
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // 1 second
    
    private final MetricsCollector metricsCollector;
    
    public PerformanceMonitoringFilter() {
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record metrics
            metricsCollector.recordRequest(request.getPath(), request.getMethod(), duration);
            
            // Log slow requests
            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                logger.warn("Slow request detected: {} {} took {}ms", 
                    request.getMethod(), request.getPath(), duration);
            }
            
            // Record error if status code >= 400
            if (response.getStatusCode() >= 400) {
                metricsCollector.recordError(request.getPath(), request.getMethod(), response.getStatusCode());
            }
        }
    }
}

