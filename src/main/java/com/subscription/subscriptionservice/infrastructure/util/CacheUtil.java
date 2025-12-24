package com.subscription.subscriptionservice.infrastructure.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.outbound.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for cache operations
 */
public class CacheUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get object from cache
     */
    public static <T> T get(CachePort cache, String key, Class<T> clazz) {
        if (cache == null) {
            return null;
        }
        
        try {
            String cached = cache.get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, clazz);
            }
        } catch (Exception e) {
            logger.warn("Error deserializing cached value: key={}", key, e);
        }
        return null;
    }
    
    /**
     * Put object in cache
     */
    public static <T> void put(CachePort cache, String key, T value) {
        put(cache, key, value, 0);
    }
    
    /**
     * Put object in cache with expiration
     */
    public static <T> void put(CachePort cache, String key, T value, long expirationSeconds) {
        if (cache == null) {
            return;
        }
        
        try {
            String json = objectMapper.writeValueAsString(value);
            cache.put(key, json, expirationSeconds);
        } catch (Exception e) {
            logger.warn("Error serializing value for cache: key={}", key, e);
        }
    }
    
    /**
     * Build cache key
     */
    public static String buildKey(String prefix, Object... parts) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object part : parts) {
            if (part != null) {
                key.append(":").append(part.toString());
            }
        }
        return key.toString();
    }
}

