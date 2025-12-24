package com.subscription.subscriptionservice.application.port.outbound;

/**
 * Port for transaction management
 */
public interface TransactionManager {
    /**
     * Execute operation in a transaction
     * @param operation The operation to execute
     * @return Result of the operation
     */
    <T> T executeInTransaction(java.util.function.Supplier<T> operation);
    
    /**
     * Execute operation in a transaction (void)
     * @param operation The operation to execute
     */
    void executeInTransaction(Runnable operation);
}

