package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.DeviceServicePort;
import com.subscription.subscriptionservice.application.port.inbound.UserDeviceServicePort;
import com.subscription.subscriptionservice.domain.model.Device;
import com.subscription.subscriptionservice.domain.model.UserDevice;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.ErrorResponse;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DeviceVerificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceVerificationController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final MetricsCollector metricsCollector;
    
    public DeviceVerificationController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.metricsCollector = MetricsCollector.getInstance();
    }
    
    public HttpResponse verifySubscription(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String apiKey = request.getHeader("X-API-Key");
            if (apiKey == null) {
                return HttpResponse.unauthorized("{\"error\":\"API key is required\"}");
            }
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.findByApiKey(apiKey);
            
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            String deviceSerial = body.get("deviceSerial");
            
            if (deviceSerial == null) {
                return HttpResponse.badRequest("{\"error\":\"Device serial is required\"}");
            }
            
            UserDeviceServicePort userDeviceService = container.getBean(UserDeviceServicePort.class);
            UserDevice userDevice = userDeviceService.findByDeviceSerial(deviceSerial);
            
            if (!userDevice.isActive()) {
                return HttpResponse.forbidden("{\"error\":\"Device is not active\"}");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", true);
            response.put("deviceId", userDevice.getDeviceId());
            response.put("subscriptionId", userDevice.getSubscriptionId());
            response.put("active", userDevice.isActive());
            
            recordMetrics("POST", "/api/device/verify-subscription", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("Device verification failed", e);
            recordMetrics("POST", "/api/device/verify-subscription", System.currentTimeMillis() - startTime, 500);
            try {
                return HttpResponse.internalServerError(objectMapper.writeValueAsString(
                    new ErrorResponse(500, "VERIFICATION_ERROR", e.getMessage())));
            } catch (Exception ex) {
                return HttpResponse.internalServerError("{\"error\":\"Device verification failed\"}");
            }
        }
    }
    
    public HttpResponse deviceHealth(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String apiKey = request.getHeader("X-API-Key");
            if (apiKey == null) {
                return HttpResponse.unauthorized("{\"error\":\"API key is required\"}");
            }
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.findByApiKey(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("deviceId", device.getId());
            response.put("deviceName", device.getName());
            
            recordMetrics("GET", "/api/device/health", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/device/health", System.currentTimeMillis() - startTime, 500);
            try {
                return HttpResponse.internalServerError(objectMapper.writeValueAsString(
                    new ErrorResponse(500, "HEALTH_CHECK_ERROR", e.getMessage())));
            } catch (Exception ex) {
                return HttpResponse.internalServerError("{\"error\":\"Health check failed\"}");
            }
        }
    }
    
    public HttpResponse deviceInfo(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String apiKey = request.getHeader("X-API-Key");
            if (apiKey == null) {
                return HttpResponse.unauthorized("{\"error\":\"API key is required\"}");
            }
            
            DeviceServicePort deviceService = container.getBean(DeviceServicePort.class);
            Device device = deviceService.findByApiKey(apiKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", device.getId());
            response.put("name", device.getName());
            response.put("description", device.getDescription());
            response.put("deviceType", device.getDeviceType());
            response.put("active", device.isActive());
            
            recordMetrics("GET", "/api/device/info", System.currentTimeMillis() - startTime, 200);
            return HttpResponse.ok(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            recordMetrics("GET", "/api/device/info", System.currentTimeMillis() - startTime, 500);
            try {
                return HttpResponse.internalServerError(objectMapper.writeValueAsString(
                    new ErrorResponse(500, "INFO_ERROR", e.getMessage())));
            } catch (Exception ex) {
                return HttpResponse.internalServerError("{\"error\":\"Failed to get device info\"}");
            }
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
}

