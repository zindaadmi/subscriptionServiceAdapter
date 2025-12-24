package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for Feature - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feature {
    private Long id;
    private String name;
    private String description;
    private String featureCode;
    private Boolean active = true;
    private Boolean deleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Domain methods
    public boolean isActive() {
        return active && !deleted;
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.active = false;
    }

    public void restore() {
        this.deleted = false;
    }
}

