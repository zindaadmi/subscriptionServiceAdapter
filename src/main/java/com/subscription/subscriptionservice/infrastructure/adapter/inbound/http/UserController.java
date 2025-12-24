package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.AuthServicePort;
import com.subscription.subscriptionservice.application.port.inbound.UserServicePort;
import com.subscription.subscriptionservice.application.port.inbound.UserSubscriptionServicePort;
import com.subscription.subscriptionservice.domain.model.User;
import com.subscription.subscriptionservice.domain.model.UserSubscription;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.UserResponse;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final ErrorHandler errorHandler;
    private final MetricsCollector metricsCollector;
    
    public UserController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.errorHandler = new ErrorHandler(objectMapper);
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse getProfile(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User user = userService.findByUsername(username);
            UserResponse response = mapToUserResponse(user);
            recordMetrics("GET", "/api/user/profile", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/user/profile", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse updateProfile(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String username = request.getHeader("X-Username");
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            User user = userService.findByUsername(username);
            
            User updated = userService.updateUserProfile(
                user.getId(),
                body.get("email"),
                body.get("phoneNumber"),
                body.get("address"),
                body.get("city"),
                body.get("state"),
                body.get("zipCode"),
                body.get("country")
            );
            
            UserResponse response = mapToUserResponse(updated);
            recordMetrics("PUT", "/api/user/profile", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("PUT", "/api/user/profile", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getMySubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User user = userService.findByUsername(username);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            List<UserSubscription> subscriptions = userSubscriptionService.findByUserId(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/user/subscriptions", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/user/subscriptions", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse getActiveSubscriptions(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String username = request.getHeader("X-Username");
            UserServicePort userService = container.getBean(UserServicePort.class);
            User user = userService.findByUsername(username);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            List<UserSubscription> subscriptions = userSubscriptionService.findByUserIdAndStatus(
                user.getId(), UserSubscription.SubscriptionStatus.ACTIVE);
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());
            
            recordMetrics("GET", "/api/user/subscriptions/active", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/user/subscriptions/active", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    public HttpResponse cancelMySubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String username = request.getHeader("X-Username");
            String subscriptionIdStr = request.getPathParams().get("subscriptionId");
            Long subscriptionId = Long.parseLong(subscriptionIdStr);
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            User user = userService.findByUsername(username);
            
            UserSubscriptionServicePort userSubscriptionService = container.getBean(UserSubscriptionServicePort.class);
            userSubscriptionService.cancelUserSubscription(user.getId(), subscriptionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription cancelled successfully");
            
            recordMetrics("POST", "/api/user/subscriptions/{subscriptionId}/cancel", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("POST", "/api/user/subscriptions/{subscriptionId}/cancel", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setCity(user.getCity());
        response.setState(user.getState());
        response.setZipCode(user.getZipCode());
        response.setCountry(user.getCountry());
        response.setEnabled(user.getEnabled());
        
        List<String> roles = new ArrayList<>();
        for (String roleName : user.getRoleNames()) {
            roles.add(roleName);
        }
        response.setRoles(roles);
        return response;
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

