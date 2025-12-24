package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for Subscription - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    private Long id;
    private String name;
    private String description;
    private Long deviceId; // Reference to device, not object
    private BigDecimal basePrice;
    private SubscriptionLevel level = SubscriptionLevel.BASIC;
    private BillingCycle billingCycle = BillingCycle.MONTHLY;
    private Boolean active = true;
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private List<Long> featureIds = new ArrayList<>(); // Reference IDs, not objects
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum SubscriptionLevel {
        BASIC,
        STANDARD,
        PREMIUM,
        ENTERPRISE
    }

    public enum BillingCycle {
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    // Domain methods
    public boolean isActive() {
        return active && !deleted;
    }

    public void markAsDeleted(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.active = false;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}

