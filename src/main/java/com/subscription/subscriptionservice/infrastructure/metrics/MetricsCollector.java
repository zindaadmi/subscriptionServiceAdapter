package com.subscription.subscriptionservice.infrastructure.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple Metrics Collector
 * Tracks request counts, response times, and errors
 */
public class MetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    private static final MetricsCollector instance = new MetricsCollector();
    
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalResponseTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestCount = new ConcurrentHashMap<>();
    
    private MetricsCollector() {
    }
    
    public static MetricsCollector getInstance() {
        return instance;
    }
    
    public void recordRequest(String endpoint, String method, long responseTime) {
        String key = method + ":" + endpoint;
        requestCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        totalResponseTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(responseTime);
        requestCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.debug("Metric recorded: {} - {}ms", key, responseTime);
    }
    
    public void recordError(String endpoint, String method, int statusCode) {
        if (statusCode >= 400) {
            String key = method + ":" + endpoint;
            errorCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            logger.debug("Error recorded: {} - Status: {}", key, statusCode);
        }
    }
    
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        Map<String, Object> requests = new ConcurrentHashMap<>();
        for (Map.Entry<String, AtomicLong> entry : requestCounts.entrySet()) {
            Map<String, Object> endpointMetrics = new ConcurrentHashMap<>();
            String key = entry.getKey();
            long count = entry.getValue().get();
            long totalTime = totalResponseTime.getOrDefault(key, new AtomicLong(0)).get();
            long errors = errorCounts.getOrDefault(key, new AtomicLong(0)).get();
            
            endpointMetrics.put("count", count);
            endpointMetrics.put("avgResponseTime", count > 0 ? totalTime / count : 0);
            endpointMetrics.put("errors", errors);
            endpointMetrics.put("errorRate", count > 0 ? (double) errors / count * 100 : 0);
            
            requests.put(key, endpointMetrics);
        }
        
        metrics.put("requests", requests);
        metrics.put("timestamp", System.currentTimeMillis());
        
        return metrics;
    }
    
    public void reset() {
        requestCounts.clear();
        errorCounts.clear();
        totalResponseTime.clear();
        requestCount.clear();
    }
}

