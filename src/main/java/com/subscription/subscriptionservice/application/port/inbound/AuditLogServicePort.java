package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogServicePort {
    void log(AuditLog auditLog);
    AuditLog findById(Long id);
    List<AuditLog> findAll(int page, int size);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findFailed();
    List<AuditLog> search(String keyword);
    Map<String, Object> getStatistics();
}

