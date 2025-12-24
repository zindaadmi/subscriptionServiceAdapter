package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for AuditLog - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private Long userId;
    private String username;
    private String userRole;
    private String description;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private String requestMethod;
    private String requestPath;
    private LocalDateTime timestamp;
    private Boolean success = true;
    private String errorMessage;

    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        SOFT_DELETE,
        RESTORE,
        LOGIN,
        LOGOUT,
        ASSIGN,
        CANCEL,
        PAY,
        VERIFY
    }

    // Domain methods
    public void markAsFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return success;
    }
}

