package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * JDBC implementation of TransactionManager
 */
public class JdbcTransactionManager implements TransactionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(JdbcTransactionManager.class);
    
    private final DataSource dataSource;
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    
    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public <T> T executeInTransaction(Supplier<T> operation) {
        Connection connection = getConnection();
        boolean wasInTransaction = connectionHolder.get() != null;
        
        try {
            if (!wasInTransaction) {
                connection.setAutoCommit(false);
                connectionHolder.set(connection);
            }
            
            T result = operation.get();
            
            if (!wasInTransaction) {
                connection.commit();
                logger.debug("Transaction committed successfully");
            }
            
            return result;
            
        } catch (Exception e) {
            if (!wasInTransaction && connection != null) {
                try {
                    connection.rollback();
                    logger.warn("Transaction rolled back due to error: {}", e.getMessage());
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction", rollbackEx);
                }
            }
            throw new RuntimeException("Transaction failed", e);
            
        } finally {
            if (!wasInTransaction) {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.setAutoCommit(true);
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
                connectionHolder.remove();
            }
        }
    }
    
    @Override
    public void executeInTransaction(Runnable operation) {
        executeInTransaction(() -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * Get current connection or create new one
     */
    private Connection getConnection() {
        Connection connection = connectionHolder.get();
        if (connection != null) {
            return connection;
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    /**
     * Get current transaction connection (for use in repositories)
     */
    public static Connection getCurrentConnection() {
        return connectionHolder.get();
    }
}

