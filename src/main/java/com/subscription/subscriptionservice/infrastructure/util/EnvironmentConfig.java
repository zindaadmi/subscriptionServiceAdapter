package com.subscription.subscriptionservice.infrastructure.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Environment-based configuration utility
 * Reads from environment variables with fallback to system properties
 */
public class EnvironmentConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    
    /**
     * Get configuration value from environment variable or system property
     */
    public static String get(String key, String defaultValue) {
        // Try environment variable first
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            logger.debug("Using environment variable for {}: {}", key, maskSensitive(key) ? "***" : value);
            return value;
        }
        
        // Try system property
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            logger.debug("Using system property for {}: {}", key, maskSensitive(key) ? "***" : value);
            return value;
        }
        
        // Return default
        logger.debug("Using default value for {}", key);
        return defaultValue;
    }
    
    /**
     * Get integer configuration value
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}, using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * Get boolean configuration value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    /**
     * Check if running in production environment
     */
    public static boolean isProduction() {
        String env = get("ENVIRONMENT", "development").toLowerCase();
        return "production".equals(env) || "prod".equals(env);
    }
    
    /**
     * Check if running in development environment
     */
    public static boolean isDevelopment() {
        String env = get("ENVIRONMENT", "development").toLowerCase();
        return "development".equals(env) || "dev".equals(env);
    }
    
    /**
     * Mask sensitive configuration keys in logs
     */
    private static boolean maskSensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password") || 
               lowerKey.contains("secret") || 
               lowerKey.contains("key") || 
               lowerKey.contains("token");
    }
}

