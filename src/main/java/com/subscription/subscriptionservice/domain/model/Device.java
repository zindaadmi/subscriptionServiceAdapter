package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for Device - Pure POJO, no framework dependencies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private Long id;
    private String name;
    private String description;
    private String deviceType;
    private Boolean active = true;
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private String apiKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

