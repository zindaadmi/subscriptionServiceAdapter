package com.subscription.subscriptionservice.infrastructure.adapter.outbound.cache;

import com.subscription.subscriptionservice.application.port.outbound.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Redis implementation of CachePort
 */
public class RedisCacheAdapter implements CachePort {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);
    
    private final JedisPool jedisPool;
    private final String keyPrefix;
    
    public RedisCacheAdapter(String host, int port, String password, String keyPrefix, int maxConnections) {
        this.keyPrefix = keyPrefix != null ? keyPrefix : "subscription:";
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxConnections);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        
        if (password != null && !password.isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000);
        }
        
        logger.info("Redis cache adapter initialized: host={}, port={}, keyPrefix={}", host, port, this.keyPrefix);
    }
    
    private String buildKey(String key) {
        return keyPrefix + key;
    }
    
    @Override
    public String get(String key) {
        String fullKey = buildKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(fullKey);
            if (value != null) {
                logger.debug("Cache hit: key={}", key);
            } else {
                logger.debug("Cache miss: key={}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("Error getting value from cache: key={}", key, e);
            return null; // Fail gracefully - return null on error
        }
    }
    
    @Override
    public void put(String key, String value) {
        put(key, value, 0); // No expiration by default
    }
    
    @Override
    public void put(String key, String value, long expirationSeconds) {
        String fullKey = buildKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            if (expirationSeconds > 0) {
                jedis.setex(fullKey, (int) expirationSeconds, value);
                logger.debug("Cached value with expiration: key={}, expiration={}s", key, expirationSeconds);
            } else {
                jedis.set(fullKey, value);
                logger.debug("Cached value: key={}", key);
            }
        } catch (Exception e) {
            logger.error("Error putting value in cache: key={}", key, e);
            // Fail gracefully - don't throw exception
        }
    }
    
    @Override
    public void delete(String key) {
        String fullKey = buildKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(fullKey);
            logger.debug("Deleted from cache: key={}", key);
        } catch (Exception e) {
            logger.error("Error deleting value from cache: key={}", key, e);
            // Fail gracefully
        }
    }
    
    @Override
    public boolean exists(String key) {
        String fullKey = buildKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(fullKey);
        } catch (Exception e) {
            logger.error("Error checking cache existence: key={}", key, e);
            return false; // Fail gracefully
        }
    }
    
    @Override
    public void expire(String key, long expirationSeconds) {
        String fullKey = buildKey(key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(fullKey, (int) expirationSeconds);
            logger.debug("Set expiration for key: key={}, expiration={}s", key, expirationSeconds);
        } catch (Exception e) {
            logger.error("Error setting expiration: key={}", key, e);
            // Fail gracefully
        }
    }
    
    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            // Delete all keys with prefix
            String pattern = keyPrefix + "*";
            var keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                logger.info("Cleared cache: {} keys deleted", keys.size());
            }
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
            // Fail gracefully
        }
    }
    
    /**
     * Close the connection pool
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }
}

