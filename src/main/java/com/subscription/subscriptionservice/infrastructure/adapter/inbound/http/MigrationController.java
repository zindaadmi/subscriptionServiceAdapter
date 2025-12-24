package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.*;
import com.subscription.subscriptionservice.domain.model.*;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import com.subscription.subscriptionservice.infrastructure.util.RoleChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public MigrationController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse importUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            String username = (String) body.get("username");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String mobileNumber = (String) body.get("mobileNumber");
            
            User user = userService.registerUser(username, email, password, mobileNumber);
            
            recordMetrics("POST", "/api/admin/migration/users", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(user));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/users", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse importDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.createDevice(
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("deviceType")
            );
            
            recordMetrics("POST", "/api/admin/migration/devices", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(device));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/devices", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse importFeature(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            FeatureServicePort featureService = container.getBean(FeatureServicePort.class);
            Feature feature = featureService.createFeature(
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("featureCode")
            );
            
            recordMetrics("POST", "/api/admin/migration/features", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(feature));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/features", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse importSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            Subscription subscription = subscriptionService.createSubscription(
                (String) body.get("name"),
                (String) body.get("description"),
                ((Number) body.get("deviceId")).longValue(),
                new BigDecimal(body.get("basePrice").toString()),
                Subscription.SubscriptionLevel.valueOf((String) body.get("level")),
                Subscription.BillingCycle.valueOf((String) body.get("billingCycle")),
                (List<Long>) body.get("featureIds")
            );
            
            recordMetrics("POST", "/api/admin/migration/subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(subscription));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse importUserSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            UserSubscription userSubscription = userSubscriptionService.assignSubscription(
                ((Number) body.get("userId")).longValue(),
                ((Number) body.get("subscriptionId")).longValue(),
                new BigDecimal(body.get("negotiatedPrice").toString()),
                ((Number) body.get("durationMonths")).intValue(),
                body.get("assignedBy") != null ? ((Number) body.get("assignedBy")).longValue() : null
            );
            
            recordMetrics("POST", "/api/admin/migration/user-subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userSubscription));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/user-subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse importUserDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            UserDevice userDevice = userDeviceService.assignDevice(
                ((Number) body.get("userId")).longValue(),
                ((Number) body.get("deviceId")).longValue(),
                ((Number) body.get("subscriptionId")).longValue(),
                ((Number) body.get("userSubscriptionId")).longValue(),
                (String) body.get("deviceSerial")
            );
            
            recordMetrics("POST", "/api/admin/migration/user-devices", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userDevice));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/user-devices", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse bulkImportUsers(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            List<Map<String, Object>> users = (List<Map<String, Object>>) body.get("users");
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            List<User> importedUsers = new java.util.ArrayList<>();
            
            for (Map<String, Object> userData : users) {
                User user = userService.registerUser(
                    (String) userData.get("username"),
                    (String) userData.get("email"),
                    (String) userData.get("password"),
                    (String) userData.get("mobileNumber")
                );
                importedUsers.add(user);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("imported", importedUsers.size());
            response.put("users", importedUsers);
            
            recordMetrics("POST", "/api/admin/migration/bulk/users", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/migration/bulk/users", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

