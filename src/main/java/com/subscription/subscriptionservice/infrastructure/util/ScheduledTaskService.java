package com.subscription.subscriptionservice.infrastructure.util;

import com.subscription.subscriptionservice.application.port.inbound.BillingServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled Task Service - Runs periodic tasks for billing and subscription management
 * 
 * Tasks:
 * 1. Generate monthly bills (runs daily at 2 AM)
 * 2. Mark overdue bills (runs daily at 3 AM)
 * 3. Suspend subscriptions with overdue bills (runs daily at 4 AM)
 */
public class ScheduledTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
    
    private final BillingServicePort billingService;
    private final ScheduledExecutorService scheduler;
    private final boolean enabled;
    
    public ScheduledTaskService(BillingServicePort billingService, boolean enabled) {
        this.billingService = billingService;
        this.enabled = enabled;
        this.scheduler = Executors.newScheduledThreadPool(3);
    }
    
    /**
     * Start all scheduled tasks
     */
    public void start() {
        if (!enabled) {
            logger.info("Scheduled tasks are disabled");
            return;
        }
        
        logger.info("Starting scheduled tasks...");
        
        // Task 1: Generate monthly bills - runs daily at 2 AM, but only executes on last day of month
        // Calculate delay to next 2 AM
        long delayTo2AM = calculateDelayToHour(2);
        scheduler.scheduleAtFixedRate(
            this::generateMonthlyBills,
            delayTo2AM,
            24 * 60 * 60 * 1000, // 24 hours
            TimeUnit.MILLISECONDS
        );
        logger.info("Scheduled: Generate monthly bills (daily at 2 AM, executes on last day of month)");
        
        // Task 2: Mark overdue bills - runs daily at 3 AM
        long delayTo3AM = calculateDelayToHour(3);
        scheduler.scheduleAtFixedRate(
            this::markOverdueBills,
            delayTo3AM,
            24 * 60 * 60 * 1000, // 24 hours
            TimeUnit.MILLISECONDS
        );
        logger.info("Scheduled: Mark overdue bills (daily at 3 AM)");
        
        // Task 3: Suspend subscriptions with overdue bills - runs daily at 4 AM
        long delayTo4AM = calculateDelayToHour(4);
        scheduler.scheduleAtFixedRate(
            this::suspendSubscriptionsWithOverdueBills,
            delayTo4AM,
            24 * 60 * 60 * 1000, // 24 hours
            TimeUnit.MILLISECONDS
        );
        logger.info("Scheduled: Suspend subscriptions with overdue bills (daily at 4 AM)");
        
        logger.info("All scheduled tasks started successfully");
    }
    
    /**
     * Stop all scheduled tasks
     */
    public void stop() {
        logger.info("Stopping scheduled tasks...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Scheduled tasks stopped");
    }
    
    /**
     * Calculate delay in milliseconds to the next occurrence of the specified hour
     */
    private long calculateDelayToHour(int targetHour) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime target = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0);
        
        if (target.isBefore(now) || target.equals(now)) {
            target = target.plusDays(1);
        }
        
        return java.time.Duration.between(now, target).toMillis();
    }
    
    /**
     * Task: Generate monthly bills for all active subscriptions
     * Runs on the last day of each month at 2 AM
     */
    private void generateMonthlyBills() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
            
            // Only generate bills on the last day of the month
            if (today.equals(lastDayOfMonth)) {
                logger.info("Running scheduled task: Generate monthly bills (Last day of month)");
                billingService.generateMonthlyBills();
                logger.info("Completed scheduled task: Generate monthly bills");
            } else {
                logger.debug("Skipping bill generation - not the last day of month. Today: {}, Last day: {}", 
                    today, lastDayOfMonth);
            }
        } catch (Exception e) {
            logger.error("Error in scheduled task: Generate monthly bills", e);
        }
    }
    
    /**
     * Task: Mark overdue bills
     */
    private void markOverdueBills() {
        try {
            logger.info("Running scheduled task: Mark overdue bills");
            billingService.markOverdueBills();
            logger.info("Completed scheduled task: Mark overdue bills");
        } catch (Exception e) {
            logger.error("Error in scheduled task: Mark overdue bills", e);
        }
    }
    
    /**
     * Task: Suspend subscriptions with overdue bills
     */
    private void suspendSubscriptionsWithOverdueBills() {
        try {
            logger.info("Running scheduled task: Suspend subscriptions with overdue bills");
            billingService.suspendSubscriptionsWithOverdueBills();
            logger.info("Completed scheduled task: Suspend subscriptions with overdue bills");
        } catch (Exception e) {
            logger.error("Error in scheduled task: Suspend subscriptions with overdue bills", e);
        }
    }
}


