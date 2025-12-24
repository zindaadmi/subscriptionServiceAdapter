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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public AdminController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse getDashboard(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            UserServicePort userService = container.getBean(UserServicePort.class);
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalUsers", userService.getAllUsers(false).size());
            dashboard.put("totalDevices", deviceService.findAll().size());
            dashboard.put("totalSubscriptions", subscriptionService.findAll().size());
            dashboard.put("activeSubscriptions", subscriptionService.findActive().size());
            
            recordMetrics("GET", "/api/admin/dashboard", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(dashboard));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/dashboard", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse softDeleteUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User admin = userService.findByUsername(username);
            
            User deleted = userService.softDeleteUser(id, admin.getId());
            
            recordMetrics("POST", "/api/admin/users/{id}/soft-delete", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(deleted));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/users/{id}/soft-delete", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse restoreUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            User restored = userService.restoreUser(id);
            
            recordMetrics("POST", "/api/admin/users/{id}/restore", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(restored));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/users/{id}/restore", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDeletedUsers(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            UserServicePort userService = container.getBean(UserServicePort.class);
            List<User> users = userService.getDeletedUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("count", users.size());
            
            recordMetrics("GET", "/api/admin/users/deleted", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/users/deleted", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse softDeleteDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User admin = userService.findByUsername(username);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            deviceService.softDeleteDevice(id, admin.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device soft deleted successfully");
            
            recordMetrics("POST", "/api/admin/devices/{id}/soft-delete", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/devices/{id}/soft-delete", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse restoreDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            deviceService.restoreDevice(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device restored successfully");
            
            recordMetrics("POST", "/api/admin/devices/{id}/restore", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/devices/{id}/restore", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDeletedDevices(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            List<Device> devices = deviceService.findDeleted();
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devices);
            response.put("count", devices.size());
            
            recordMetrics("GET", "/api/admin/devices/deleted", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/devices/deleted", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse softDeleteSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User admin = userService.findByUsername(username);
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            subscriptionService.softDeleteSubscription(id, admin.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription soft deleted successfully");
            
            recordMetrics("POST", "/api/admin/subscriptions/{id}/soft-delete", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/subscriptions/{id}/soft-delete", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse restoreSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            subscriptionService.restoreSubscription(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription restored successfully");
            
            recordMetrics("POST", "/api/admin/subscriptions/{id}/restore", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/subscriptions/{id}/restore", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDeletedSubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            List<Subscription> subscriptions = subscriptionService.findDeleted();
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/admin/subscriptions/deleted", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/subscriptions/deleted", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse createFeature(HttpRequest request) {
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
            
            recordMetrics("POST", "/api/admin/features", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(feature));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/features", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAllFeatures(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            FeatureServicePort featureService = container.getBean(FeatureServicePort.class);
            List<Feature> features = featureService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("features", features);
            response.put("count", features.size());
            
            recordMetrics("GET", "/api/admin/features", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/features", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getActiveFeatures(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            FeatureServicePort featureService = container.getBean(FeatureServicePort.class);
            List<Feature> features = featureService.findActive();
            
            Map<String, Object> response = new HashMap<>();
            response.put("features", features);
            response.put("count", features.size());
            
            recordMetrics("GET", "/api/admin/features/active", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/admin/features/active", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse addFeaturesToSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String subscriptionIdStr = request.getPathParams().get("subscriptionId");
            Long subscriptionId = Long.parseLong(subscriptionIdStr);
            
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            List<Long> featureIds = (List<Long>) body.get("featureIds");
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            subscriptionService.addFeatures(subscriptionId, featureIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Features added successfully");
            
            recordMetrics("POST", "/api/admin/subscriptions/{subscriptionId}/features", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/subscriptions/{subscriptionId}/features", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse removeFeaturesFromSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            String subscriptionIdStr = request.getPathParams().get("subscriptionId");
            Long subscriptionId = Long.parseLong(subscriptionIdStr);
            
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            List<Long> featureIds = (List<Long>) body.get("featureIds");
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            subscriptionService.removeFeatures(subscriptionId, featureIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Features removed successfully");
            
            recordMetrics("DELETE", "/api/admin/subscriptions/{subscriptionId}/features", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("DELETE", "/api/admin/subscriptions/{subscriptionId}/features", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse createSubscription(HttpRequest request) {
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
            
            recordMetrics("POST", "/api/admin/subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(subscription));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse assignSubscriptionToUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User admin = userService.findByUsername(username);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            UserSubscription userSubscription = userSubscriptionService.assignSubscription(
                ((Number) body.get("userId")).longValue(),
                ((Number) body.get("subscriptionId")).longValue(),
                new BigDecimal(body.get("negotiatedPrice").toString()),
                ((Number) body.get("durationMonths")).intValue(),
                admin.getId()
            );
            
            recordMetrics("POST", "/api/admin/user-subscriptions/assign", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userSubscription));
        } catch (Exception e) {
            recordMetrics("POST", "/api/admin/user-subscriptions/assign", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

