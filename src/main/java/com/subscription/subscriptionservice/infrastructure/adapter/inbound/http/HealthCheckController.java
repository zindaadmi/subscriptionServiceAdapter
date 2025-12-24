package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.outbound.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides detailed health information
 */
public class HealthCheckController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    
    public HealthCheckController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
    }
    
    public HttpResponse basicHealth(HttpRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "subscription-service");
            response.put("timestamp", System.currentTimeMillis());
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("Error in basic health check", e);
            return HttpResponse.serverError("{\"status\":\"ERROR\"}");
        }
    }
    
    public HttpResponse detailedHealth(HttpRequest request) {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "subscription-service");
            health.put("timestamp", System.currentTimeMillis());
            
            Map<String, Object> components = new HashMap<>();
            
            // Database health
            components.put("database", checkDatabase());
            
            // Connection pool health
            components.put("connectionPool", checkConnectionPool());
            
            // Redis health
            components.put("cache", checkCache());
            
            // Memory health
            components.put("memory", checkMemory());
            
            // Disk health
            components.put("disk", checkDisk());
            
            health.put("components", components);
            
            // Overall status
            boolean allHealthy = components.values().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .allMatch(c -> "UP".equals(c.get("status")));
            
            if (!allHealthy) {
                health.put("status", "DEGRADED");
            }
            
            return HttpResponse.ok(objectMapper.writeValueAsString(health));
        } catch (Exception e) {
            logger.error("Error in detailed health check", e);
            return HttpResponse.serverError("{\"status\":\"ERROR\"}");
        }
    }
    
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        try {
            DataSource dataSource = container.getBean(DataSource.class);
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    boolean valid = conn.isValid(2); // 2 second timeout
                    dbHealth.put("status", valid ? "UP" : "DOWN");
                    dbHealth.put("message", valid ? "Connection successful" : "Connection failed");
                }
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("message", "DataSource not available");
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            dbHealth.put("status", "DOWN");
            dbHealth.put("message", "Error: " + e.getMessage());
        }
        return dbHealth;
    }
    
    private Map<String, Object> checkConnectionPool() {
        Map<String, Object> poolHealth = new HashMap<>();
        try {
            DataSource dataSource = container.getBean(DataSource.class);
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
                
                if (poolBean != null) {
                    int active = poolBean.getActiveConnections();
                    int idle = poolBean.getIdleConnections();
                    int total = poolBean.getTotalConnections();
                    int threadsAwaiting = poolBean.getThreadsAwaitingConnection();
                    int maxPoolSize = hikariDS.getMaximumPoolSize();
                    
                    double usagePercent = maxPoolSize > 0 ? (double) active / maxPoolSize * 100 : 0;
                    
                    poolHealth.put("status", usagePercent > 90 ? "WARNING" : "UP");
                    poolHealth.put("active", active);
                    poolHealth.put("idle", idle);
                    poolHealth.put("total", total);
                    poolHealth.put("max", maxPoolSize);
                    poolHealth.put("threadsAwaiting", threadsAwaiting);
                    poolHealth.put("usagePercent", String.format("%.2f", usagePercent));
                    
                    if (usagePercent > 90) {
                        poolHealth.put("message", "Connection pool usage is high: " + String.format("%.2f", usagePercent) + "%");
                    } else if (threadsAwaiting > 0) {
                        poolHealth.put("message", "Threads waiting for connections: " + threadsAwaiting);
                    } else {
                        poolHealth.put("message", "Connection pool is healthy");
                    }
                } else {
                    poolHealth.put("status", "UNKNOWN");
                    poolHealth.put("message", "Pool metrics not available");
                }
            } else {
                poolHealth.put("status", "UNKNOWN");
                poolHealth.put("message", "Not using HikariCP");
            }
        } catch (Exception e) {
            logger.error("Connection pool health check failed", e);
            poolHealth.put("status", "ERROR");
            poolHealth.put("message", "Error: " + e.getMessage());
        }
        return poolHealth;
    }
    
    private Map<String, Object> checkCache() {
        Map<String, Object> cacheHealth = new HashMap<>();
        try {
            CachePort cachePort;
            try {
                cachePort = container.getBean(CachePort.class);
            } catch (Exception e) {
                cachePort = null;
            }
            
            if (cachePort != null) {
                // Try to get a test key
                String testKey = "health:check:" + System.currentTimeMillis();
                cachePort.put(testKey, "test", 10);
                String value = cachePort.get(testKey);
                boolean healthy = "test".equals(value);
                cachePort.delete(testKey);
                
                cacheHealth.put("status", healthy ? "UP" : "DOWN");
                cacheHealth.put("message", healthy ? "Cache operational" : "Cache not responding");
            } else {
                cacheHealth.put("status", "DISABLED");
                cacheHealth.put("message", "Cache not configured");
            }
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            cacheHealth.put("status", "DOWN");
            cacheHealth.put("message", "Error: " + e.getMessage());
        }
        return cacheHealth;
    }
    
    private Map<String, Object> checkMemory() {
        Map<String, Object> memoryHealth = new HashMap<>();
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usagePercent = (double) usedMemory / maxMemory * 100;
            
            memoryHealth.put("status", usagePercent > 90 ? "WARNING" : "UP");
            memoryHealth.put("max", maxMemory);
            memoryHealth.put("used", usedMemory);
            memoryHealth.put("free", freeMemory);
            memoryHealth.put("usagePercent", String.format("%.2f", usagePercent));
            
            if (usagePercent > 90) {
                memoryHealth.put("message", "Memory usage is high: " + String.format("%.2f", usagePercent) + "%");
            } else {
                memoryHealth.put("message", "Memory usage is normal");
            }
        } catch (Exception e) {
            logger.error("Memory health check failed", e);
            memoryHealth.put("status", "ERROR");
            memoryHealth.put("message", "Error: " + e.getMessage());
        }
        return memoryHealth;
    }
    
    private Map<String, Object> checkDisk() {
        Map<String, Object> diskHealth = new HashMap<>();
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            
            diskHealth.put("status", usagePercent > 90 ? "WARNING" : "UP");
            diskHealth.put("total", totalSpace);
            diskHealth.put("free", freeSpace);
            diskHealth.put("used", usedSpace);
            diskHealth.put("usagePercent", String.format("%.2f", usagePercent));
            
            if (usagePercent > 90) {
                diskHealth.put("message", "Disk usage is high: " + String.format("%.2f", usagePercent) + "%");
            } else {
                diskHealth.put("message", "Disk usage is normal");
            }
        } catch (Exception e) {
            logger.error("Disk health check failed", e);
            diskHealth.put("status", "ERROR");
            diskHealth.put("message", "Error: " + e.getMessage());
        }
        return diskHealth;
    }
}

