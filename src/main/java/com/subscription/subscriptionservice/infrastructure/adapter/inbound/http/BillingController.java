package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.BillingServicePort;
import com.subscription.subscriptionservice.domain.model.Billing;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import com.subscription.subscriptionservice.infrastructure.util.RoleChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public BillingController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse generateMonthlyBills(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            billingService.generateMonthlyBills();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Monthly bills generated successfully");
            
            recordMetrics("POST", "/api/billing/generate-monthly", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/billing/generate-monthly", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse generateBill(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String userSubscriptionIdStr = request.getPathParams().get("userSubscriptionId");
            Long userSubscriptionId = Long.parseLong(userSubscriptionIdStr);
            
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            LocalDate periodStart = body.get("billingPeriodStart") != null ? 
                LocalDate.parse(body.get("billingPeriodStart")) : LocalDate.now();
            LocalDate periodEnd = body.get("billingPeriodEnd") != null ? 
                LocalDate.parse(body.get("billingPeriodEnd")) : periodStart.plusMonths(1);
            
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            Billing billing = billingService.generateBill(userSubscriptionId, periodStart, periodEnd);
            
            recordMetrics("POST", "/api/billing/generate/{userSubscriptionId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(billing));
        } catch (Exception e) {
            recordMetrics("POST", "/api/billing/generate/{userSubscriptionId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getPendingBills(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            List<Billing> billings = billingService.findPending();
            
            Map<String, Object> response = new HashMap<>();
            response.put("bills", billings);
            response.put("count", billings.size());
            
            recordMetrics("GET", "/api/billing/pending", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/billing/pending", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getBillsBySubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String userSubscriptionIdStr = request.getPathParams().get("userSubscriptionId");
            Long userSubscriptionId = Long.parseLong(userSubscriptionIdStr);
            
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            List<Billing> billings = billingService.findByUserSubscriptionId(userSubscriptionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bills", billings);
            response.put("count", billings.size());
            
            recordMetrics("GET", "/api/billing/user-subscription/{userSubscriptionId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/billing/user-subscription/{userSubscriptionId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse markAsPaid(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String billingIdStr = request.getPathParams().get("billingId");
            Long billingId = Long.parseLong(billingIdStr);
            
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            String paymentMethod = body.get("paymentMethod");
            
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            billingService.markAsPaid(billingId, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bill marked as paid successfully");
            
            recordMetrics("PUT", "/api/billing/{billingId}/mark-paid", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("PUT", "/api/billing/{billingId}/mark-paid", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse payBill(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String billingIdStr = request.getPathParams().get("billingId");
            Long billingId = Long.parseLong(billingIdStr);
            
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            String paymentMethod = body.get("paymentMethod");
            
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            billingService.markAsPaid(billingId, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bill paid successfully");
            
            recordMetrics("PUT", "/api/billing/{billingId}/pay", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("PUT", "/api/billing/{billingId}/pay", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse markOverdue(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            BillingServicePort billingService = container.getBean(BillingServicePort.class);
            billingService.markOverdueBills();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Overdue bills marked successfully");
            
            recordMetrics("POST", "/api/billing/mark-overdue", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/billing/mark-overdue", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

