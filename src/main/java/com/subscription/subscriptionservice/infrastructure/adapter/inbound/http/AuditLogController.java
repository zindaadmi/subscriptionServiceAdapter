package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.AuditLogServicePort;
import com.subscription.subscriptionservice.domain.model.AuditLog;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import com.subscription.subscriptionservice.infrastructure.util.RoleChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditLogController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public AuditLogController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse getAllAuditLogs(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String pageStr = request.getQueryParams() != null ? request.getQueryParams().get("page") : "0";
            String sizeStr = request.getQueryParams() != null ? request.getQueryParams().get("size") : "50";
            int page = Integer.parseInt(pageStr);
            int size = Integer.parseInt(sizeStr);
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findAll(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("page", page);
            response.put("size", size);
            
            recordMetrics("GET", "/api/audit", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAuditTrail(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String entityType = request.getPathParams().get("entityType");
            String entityIdStr = request.getPathParams().get("entityId");
            Long entityId = Long.parseLong(entityIdStr);
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findByEntityTypeAndEntityId(entityType, entityId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("trail", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/trail/{entityType}/{entityId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/trail/{entityType}/{entityId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAuditLogsByUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String userIdStr = request.getPathParams().get("userId");
            Long userId = Long.parseLong(userIdStr);
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/user/{userId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/user/{userId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAuditLogsByAction(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String action = request.getPathParams().get("action");
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findByAction(action);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/action/{action}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/action/{action}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAuditLogsByEntityType(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String entityType = request.getPathParams().get("entityType");
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findByEntityType(entityType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/entity/{entityType}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/entity/{entityType}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAuditLogsByDateRange(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String startStr = request.getQueryParams() != null ? request.getQueryParams().get("start") : null;
            String endStr = request.getQueryParams() != null ? request.getQueryParams().get("end") : null;
            
            if (startStr == null || endStr == null) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Start and end dates are required");
            }
            
            LocalDateTime start = LocalDateTime.parse(startStr);
            LocalDateTime end = LocalDateTime.parse(endStr);
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findByDateRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/date-range", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/date-range", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getFailedAuditLogs(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.findFailed();
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/failed", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/failed", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse searchAuditLogs(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String keyword = request.getQueryParams() != null ? request.getQueryParams().get("keyword") : null;
            if (keyword == null || keyword.trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Search keyword is required");
            }
            
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            List<AuditLog> logs = auditLogService.search(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            
            recordMetrics("GET", "/api/audit/search", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/search", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getStatistics(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            AuditLogServicePort auditLogService = container.getBean(AuditLogServicePort.class);
            Map<String, Object> stats = auditLogService.getStatistics();
            
            recordMetrics("GET", "/api/audit/statistics", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            recordMetrics("GET", "/api/audit/statistics", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

