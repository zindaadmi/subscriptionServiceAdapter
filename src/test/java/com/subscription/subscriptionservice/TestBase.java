package com.subscription.subscriptionservice;

import com.framework.core.di.Container;
import com.framework.core.bootstrap.ApplicationBootstrap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/**
 * Base test class for integration tests
 * Sets up test environment and provides common utilities
 */
public abstract class TestBase {
    
    protected Container container;
    protected ApplicationBootstrap bootstrap;
    
    @BeforeEach
    public void setUp() {
        // Initialize test bootstrap with test configuration
        bootstrap = new ApplicationBootstrap();
        try {
            bootstrap.initialize("application-test.yml");
            container = bootstrap.getContainer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test environment", e);
        }
    }
    
    @AfterEach
    public void tearDown() {
        if (bootstrap != null) {
            try {
                bootstrap.stop();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
}

