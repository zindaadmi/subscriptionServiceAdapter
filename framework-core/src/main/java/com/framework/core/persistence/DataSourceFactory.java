package com.framework.core.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Factory for creating DataSource instances from configuration
 */
public class DataSourceFactory {
    
    public static DataSource createDataSource(Map<String, Object> config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        String type = (String) config.get("type");
        String url = (String) config.get("url");
        String driver = (String) config.get("driver");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password != null ? password : "");
        
        // Pool configuration
        @SuppressWarnings("unchecked")
        Map<String, Object> poolConfig = (Map<String, Object>) config.get("pool");
        if (poolConfig != null) {
            if (poolConfig.get("maxSize") != null) {
                hikariConfig.setMaximumPoolSize(((Number) poolConfig.get("maxSize")).intValue());
            }
            if (poolConfig.get("minIdle") != null) {
                hikariConfig.setMinimumIdle(((Number) poolConfig.get("minIdle")).intValue());
            }
            if (poolConfig.get("connectionTimeout") != null) {
                hikariConfig.setConnectionTimeout(((Number) poolConfig.get("connectionTimeout")).longValue());
            }
            if (poolConfig.get("idleTimeout") != null) {
                hikariConfig.setIdleTimeout(((Number) poolConfig.get("idleTimeout")).longValue());
            }
            if (poolConfig.get("maxLifetime") != null) {
                hikariConfig.setMaxLifetime(((Number) poolConfig.get("maxLifetime")).longValue());
            }
        }
        
        return new HikariDataSource(hikariConfig);
    }
}

