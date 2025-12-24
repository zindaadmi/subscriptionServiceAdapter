package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.AuditLogServicePort;
import com.subscription.subscriptionservice.application.port.outbound.AuditLogRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AuditLogUseCase implements AuditLogServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogUseCase.class);
    
    private final AuditLogRepositoryPort auditLogRepository;
    
    public AuditLogUseCase(AuditLogRepositoryPort auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Override
    public void log(AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Audit log not found with id: " + id));
    }
    
    @Override
    public List<AuditLog> findAll(int page, int size) {
        return auditLogRepository.findAll(page, size);
    }
    
    @Override
    public List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    @Override
    public List<AuditLog> findByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
    
    @Override
    public List<AuditLog> findByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }
    
    @Override
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByDateRange(start, end);
    }
    
    @Override
    public List<AuditLog> findFailed() {
        return auditLogRepository.findFailed();
    }
    
    @Override
    public List<AuditLog> search(String keyword) {
        return auditLogRepository.search(keyword);
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        return auditLogRepository.getStatistics();
    }
}

