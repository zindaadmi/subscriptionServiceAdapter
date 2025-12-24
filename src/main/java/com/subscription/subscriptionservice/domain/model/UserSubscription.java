package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model for UserSubscription - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {
    private Long id;
    private Long userId;
    private Long subscriptionId;
    private BigDecimal negotiatedPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate billingStartDate;
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    private Integer durationMonths;
    private Long assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELLED,
        EXPIRED,
        SUSPENDED
    }

    // Domain methods
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.endDate = LocalDate.now();
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
}

