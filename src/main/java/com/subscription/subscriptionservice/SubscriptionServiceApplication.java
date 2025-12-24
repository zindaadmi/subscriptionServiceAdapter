package com.subscription.subscriptionservice;

import com.framework.core.bootstrap.ApplicationBootstrap;
import com.framework.core.di.Container;
import com.framework.core.http.HttpServer;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.RestControllerAdapter;

/**
 * Main application entry point
 * No Spring Boot - Pure Ports and Adapters architecture
 */
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        try {
            // Initialize application bootstrap
            ApplicationBootstrap bootstrap = new ApplicationBootstrap();
            bootstrap.initialize("application.yml");
            
            // Get container
            Container container = bootstrap.getContainer();
            
            // Register filters
            HttpServer httpServer = container.getBean(HttpServer.class);
            com.subscription.subscriptionservice.application.port.outbound.SecurityPort securityPort = 
                container.getBean(com.subscription.subscriptionservice.application.port.outbound.SecurityPort.class);
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // Request ID Filter (must be first)
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestIdFilter requestIdFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestIdFilter();
            httpServer.addFilter(requestIdFilter);
            
            // Request Size Limit Filter (DoS protection)
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestSizeLimitFilter sizeLimitFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestSizeLimitFilter(10 * 1024 * 1024); // 10MB
            httpServer.addFilter(sizeLimitFilter);
            
            // Request/Response Logging Filter
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestResponseLoggingFilter loggingFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RequestResponseLoggingFilter();
            httpServer.addFilter(loggingFilter);
            
            // Performance Monitoring Filter
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.PerformanceMonitoringFilter performanceFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.PerformanceMonitoringFilter();
            httpServer.addFilter(performanceFilter);
            
            // CORS Filter
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.CorsFilter corsFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.CorsFilter();
            httpServer.addFilter(corsFilter);
            
            // Security Headers Filter
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.SecurityHeadersFilter securityHeadersFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.SecurityHeadersFilter();
            httpServer.addFilter(securityHeadersFilter);
            
            // JWT Authentication Filter
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.JwtAuthenticationFilter authFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.JwtAuthenticationFilter(
                    securityPort, objectMapper);
            httpServer.addFilter(authFilter);
            
            // Rate Limiting Filter (100 requests per minute per IP)
            com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RateLimitFilter rateLimitFilter = 
                new com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter.RateLimitFilter(
                    objectMapper, 100);
            httpServer.addFilter(rateLimitFilter);
            
            // Register REST controllers
            RestControllerAdapter restController = new RestControllerAdapter(container);
            restController.registerRoutes();
            
            // Start HTTP server
            System.out.println("Starting Subscription Service...");
            bootstrap.start();
            
            System.out.println("Subscription Service started successfully!");
            System.out.println("Server running on port 8080");
            System.out.println("Press Ctrl+C to stop");
            
            // Enhanced graceful shutdown
            com.subscription.subscriptionservice.infrastructure.util.GracefulShutdown gracefulShutdown = 
                new com.subscription.subscriptionservice.infrastructure.util.GracefulShutdown(
                    httpServer, 
                    container.getBean(javax.sql.DataSource.class),
                    30 // 30 second timeout
                );
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("\nReceived shutdown signal, initiating graceful shutdown...");
                    gracefulShutdown.shutdown();
                    System.out.println("Shutdown complete");
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                    e.printStackTrace();
                }
            }));
            
            // Wait for shutdown signal
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
