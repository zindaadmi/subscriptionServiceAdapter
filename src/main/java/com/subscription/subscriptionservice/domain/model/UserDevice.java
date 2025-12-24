package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model for UserDevice - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {
    private Long id;
    private Long userId;
    private Long deviceId;
    private Long subscriptionId;
    private Long userSubscriptionId;
    private String deviceSerial;
    private LocalDate purchaseDate;
    private Boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Domain methods
    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}

