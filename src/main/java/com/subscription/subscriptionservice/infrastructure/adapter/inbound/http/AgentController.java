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

public class AgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public AgentController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse createDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.createDevice(
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("deviceType")
            );
            
            recordMetrics("POST", "/api/agent/devices", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(device));
        } catch (Exception e) {
            recordMetrics("POST", "/api/agent/devices", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDeviceApiKey(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            String apiKey = deviceService.getApiKey(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("apiKey", apiKey);
            
            recordMetrics("GET", "/api/agent/devices/{id}/api-key", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/devices/{id}/api-key", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse regenerateApiKey(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            String newApiKey = deviceService.regenerateApiKey(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("apiKey", newApiKey);
            response.put("message", "API key regenerated successfully");
            
            recordMetrics("POST", "/api/agent/devices/{id}/regenerate-api-key", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/agent/devices/{id}/regenerate-api-key", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAllDevices(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            List<Device> devices = deviceService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devices);
            response.put("count", devices.size());
            
            recordMetrics("GET", "/api/agent/devices", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/devices", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getActiveDevices(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            List<Device> devices = deviceService.findActive();
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devices);
            response.put("count", devices.size());
            
            recordMetrics("GET", "/api/agent/devices/active", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/devices/active", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDeviceById(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.findById(id);
            
            recordMetrics("GET", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(device));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse updateDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.updateDevice(
                id,
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("deviceType")
            );
            
            recordMetrics("PUT", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(device));
        } catch (Exception e) {
            recordMetrics("PUT", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse deactivateDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            deviceService.deleteDevice(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device deactivated successfully");
            
            recordMetrics("DELETE", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("DELETE", "/api/agent/devices/{id}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAllSubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            List<Subscription> subscriptions = subscriptionService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/agent/subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getSubscriptionsByDevice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String deviceIdStr = request.getPathParams().get("deviceId");
            Long deviceId = Long.parseLong(deviceIdStr);
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            List<Subscription> subscriptions = subscriptionService.findByDeviceId(deviceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/agent/subscriptions/device/{deviceId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/subscriptions/device/{deviceId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getSubscriptionById(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            SubscriptionServicePort subscriptionService = container.getBean(SubscriptionServicePort.class);
            Subscription subscription = subscriptionService.findById(id);
            
            recordMetrics("GET", "/api/agent/subscriptions/{id}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(subscription));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/subscriptions/{id}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse assignSubscriptionToUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User agent = userService.findByUsername(username);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            UserSubscription userSubscription = userSubscriptionService.assignSubscription(
                ((Number) body.get("userId")).longValue(),
                ((Number) body.get("subscriptionId")).longValue(),
                new BigDecimal(body.get("negotiatedPrice").toString()),
                ((Number) body.get("durationMonths")).intValue(),
                agent.getId()
            );
            
            recordMetrics("POST", "/api/agent/user-subscriptions/assign", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userSubscription));
        } catch (Exception e) {
            recordMetrics("POST", "/api/agent/user-subscriptions/assign", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAllUserSubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            List<UserSubscription> subscriptions = userSubscriptionService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/agent/user-subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/user-subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getActiveUserSubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            List<UserSubscription> subscriptions = userSubscriptionService.findActive();
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/agent/user-subscriptions/active", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/user-subscriptions/active", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getUserSubscriptionsByUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String userIdStr = request.getPathParams().get("userId");
            Long userId = Long.parseLong(userIdStr);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            List<UserSubscription> subscriptions = userSubscriptionService.findByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/agent/user-subscriptions/user/{userId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/user-subscriptions/user/{userId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse updateNegotiatedPrice(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            BigDecimal negotiatedPrice = new BigDecimal(body.get("negotiatedPrice").toString());
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            UserSubscription userSubscription = userSubscriptionService.updateNegotiatedPrice(id, negotiatedPrice);
            
            recordMetrics("PUT", "/api/agent/user-subscriptions/{id}/negotiated-price", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userSubscription));
        } catch (Exception e) {
            recordMetrics("PUT", "/api/agent/user-subscriptions/{id}/negotiated-price", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse cancelSubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String idStr = request.getPathParams().get("id");
            Long id = Long.parseLong(idStr);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            userSubscriptionService.cancelSubscription(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription cancelled successfully");
            
            recordMetrics("POST", "/api/agent/user-subscriptions/{id}/cancel", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/agent/user-subscriptions/{id}/cancel", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse assignDeviceToUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            Map<String, Object> body = objectMapper.readValue(request.getBody(), Map.class);
            
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            UserDevice userDevice = userDeviceService.assignDevice(
                ((Number) body.get("userId")).longValue(),
                ((Number) body.get("deviceId")).longValue(),
                ((Number) body.get("subscriptionId")).longValue(),
                ((Number) body.get("userSubscriptionId")).longValue(),
                (String) body.get("deviceSerial")
            );
            
            recordMetrics("POST", "/api/agent/user-devices/assign", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(userDevice));
        } catch (Exception e) {
            recordMetrics("POST", "/api/agent/user-devices/assign", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getAllUserDevices(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            List<UserDevice> devices = userDeviceService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devices);
            response.put("count", devices.size());
            
            recordMetrics("GET", "/api/agent/user-devices", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/user-devices", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getUserDevicesByUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            String userIdStr = request.getPathParams().get("userId");
            Long userId = Long.parseLong(userIdStr);
            
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            List<UserDevice> devices = userDeviceService.findByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devices);
            response.put("count", devices.size());
            
            recordMetrics("GET", "/api/agent/user-devices/user/{userId}", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/user-devices/user/{userId}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getDashboard(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            RoleChecker.requireAnyRole(request, "ROLE_ADMIN", "ROLE_AGENT");
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalSubscriptions", userSubscriptionService.findAll().size());
            dashboard.put("activeSubscriptions", userSubscriptionService.findActive().size());
            dashboard.put("totalDevices", userDeviceService.findAll().size());
            
            recordMetrics("GET", "/api/agent/dashboard", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(dashboard));
        } catch (Exception e) {
            recordMetrics("GET", "/api/agent/dashboard", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

