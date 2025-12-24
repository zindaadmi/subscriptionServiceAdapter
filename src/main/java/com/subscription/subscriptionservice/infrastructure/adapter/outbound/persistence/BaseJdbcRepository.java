package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base class for JDBC repositories with common connection management
 */
public abstract class BaseJdbcRepository {
    
    protected final DataSource dataSource;
    
    protected BaseJdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    protected Connection getConnection() {
        Connection conn = JdbcTransactionManager.getCurrentConnection();
        if (conn != null) {
            return conn;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    protected boolean shouldCloseConnection() {
        return JdbcTransactionManager.getCurrentConnection() == null;
    }
    
    protected void closeConnectionIfNeeded(Connection conn, boolean shouldClose) {
        if (shouldClose && conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log but don't throw
            }
        }
    }
}

