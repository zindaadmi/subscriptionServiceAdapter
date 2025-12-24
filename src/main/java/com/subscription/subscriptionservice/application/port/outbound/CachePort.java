package com.subscription.subscriptionservice.application.port.outbound;

/**
 * Port for caching operations
 */
public interface CachePort {
    /**
     * Get value from cache
     * @param key Cache key
     * @return Cached value or null if not found
     */
    String get(String key);
    
    /**
     * Put value in cache
     * @param key Cache key
     * @param value Value to cache
     */
    void put(String key, String value);
    
    /**
     * Put value in cache with expiration
     * @param key Cache key
     * @param value Value to cache
     * @param expirationSeconds Expiration time in seconds
     */
    void put(String key, String value, long expirationSeconds);
    
    /**
     * Delete value from cache
     * @param key Cache key
     */
    void delete(String key);
    
    /**
     * Check if key exists in cache
     * @param key Cache key
     * @return true if key exists
     */
    boolean exists(String key);
    
    /**
     * Set expiration for existing key
     * @param key Cache key
     * @param expirationSeconds Expiration time in seconds
     */
    void expire(String key, long expirationSeconds);
    
    /**
     * Clear all cache
     */
    void clear();
}

