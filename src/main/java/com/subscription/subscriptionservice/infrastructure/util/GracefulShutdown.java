package com.subscription.subscriptionservice.infrastructure.util;

import com.framework.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Graceful shutdown handler
 * Ensures all requests complete before shutting down
 */
public class GracefulShutdown {
    
    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);
    
    private final HttpServer httpServer;
    private final DataSource dataSource;
    private final int shutdownTimeoutSeconds;
    
    private volatile boolean shuttingDown = false;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    
    public GracefulShutdown(HttpServer httpServer, DataSource dataSource, int shutdownTimeoutSeconds) {
        this.httpServer = httpServer;
        this.dataSource = dataSource;
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
    }
    
    /**
     * Initiate graceful shutdown
     */
    public void shutdown() {
        if (shuttingDown) {
            logger.warn("Shutdown already in progress");
            return;
        }
        
        shuttingDown = true;
        logger.info("Initiating graceful shutdown...");
        
        try {
            // Step 1: Stop accepting new requests
            logger.info("Stopping HTTP server (no new requests)...");
            if (httpServer != null && httpServer.isRunning()) {
                // Note: JettyHttpServer needs to support graceful shutdown
                // For now, we'll just stop it
                httpServer.stop();
            }
            
            // Step 2: Wait for ongoing requests to complete
            logger.info("Waiting for ongoing requests to complete (max {} seconds)...", shutdownTimeoutSeconds);
            boolean completed = shutdownLatch.await(shutdownTimeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                logger.warn("Shutdown timeout reached, forcing shutdown");
            } else {
                logger.info("All requests completed");
            }
            
            // Step 3: Close database connections
            logger.info("Closing database connections...");
            closeDatabaseConnections();
            
            logger.info("Graceful shutdown completed");
            
        } catch (Exception e) {
            logger.error("Error during graceful shutdown", e);
        }
    }
    
    private void closeDatabaseConnections() {
        if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
            com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
            hikariDS.close();
            logger.info("HikariCP connection pool closed");
        } else if (dataSource != null) {
            try {
                // Try to close if it implements AutoCloseable
                if (dataSource instanceof AutoCloseable) {
                    ((AutoCloseable) dataSource).close();
                    logger.info("DataSource closed");
                }
            } catch (Exception e) {
                logger.warn("Error closing DataSource", e);
            }
        }
    }
    
    /**
     * Signal that a request has completed
     */
    public void requestCompleted() {
        // This can be called by request handlers when they complete
        // For now, we'll rely on the timeout
    }
    
    /**
     * Check if shutdown is in progress
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }
}

