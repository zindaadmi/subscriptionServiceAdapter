package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.framework.core.http.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.inbound.AuthServicePort;
import com.subscription.subscriptionservice.application.port.inbound.UserServicePort;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.*;
import com.subscription.subscriptionservice.infrastructure.metrics.MetricsCollector;
import com.subscription.subscriptionservice.infrastructure.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller Adapter - Maps HTTP requests to use cases
 * Complete implementation with proper error handling and validation
 */
public class RestControllerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(RestControllerAdapter.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;
    private final HttpServer httpServer;
    private final ErrorHandler errorHandler;
    private final HealthCheckController healthCheckController;
    private final MetricsCollector metricsCollector;
    
    // Controller instances
    private final UserController userController;
    private final AdminController adminController;
    private final AgentController agentController;
    private final BillingController billingController;
    private final DeviceVerificationController deviceVerificationController;
    private final AuditLogController auditLogController;
    private final MigrationController migrationController;
    
    public RestControllerAdapter(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
        this.httpServer = container.getBean(HttpServer.class);
        this.errorHandler = new ErrorHandler(objectMapper);
        this.healthCheckController = new HealthCheckController(container);
        this.metricsCollector = MetricsCollector.getInstance();
        
        // Initialize controllers
        this.userController = new UserController(container);
        this.adminController = new AdminController(container);
        this.agentController = new AgentController(container);
        this.billingController = new BillingController(container);
        this.deviceVerificationController = new DeviceVerificationController(container);
        this.auditLogController = new AuditLogController(container);
        this.migrationController = new MigrationController(container);
    }
    
    public void registerRoutes() {
        // Health checks
        httpServer.addRoute("GET", "/health", this::healthCheck);
        httpServer.addRoute("GET", "/health/detailed", this::detailedHealthCheck);
        httpServer.addRoute("GET", "/metrics", this::metrics);
        
        // API Documentation
        OpenApiController openApiController = new OpenApiController();
        httpServer.addRoute("GET", "/api-docs", openApiController::getOpenApiSpec);
        httpServer.addRoute("GET", "/swagger.json", openApiController::getOpenApiSpec);
        
        // Auth routes
        httpServer.addRoute("POST", "/api/auth/register", this::register);
        httpServer.addRoute("POST", "/api/auth/login", this::login);
        httpServer.addRoute("POST", "/api/auth/login/mobile", this::loginByMobile);
        httpServer.addRoute("POST", "/api/auth/refresh", this::refreshToken);
        httpServer.addRoute("POST", "/api/auth/logout", this::logout);
        httpServer.addRoute("GET", "/api/auth/me", this::getCurrentUser);
        
        // User routes
        httpServer.addRoute("GET", "/api/users/{id}", this::getUser);
        httpServer.addRoute("GET", "/api/users", this::getAllUsers);
        
        // User Controller routes
        httpServer.addRoute("GET", "/api/user/profile", userController::getProfile);
        httpServer.addRoute("PUT", "/api/user/profile", userController::updateProfile);
        httpServer.addRoute("GET", "/api/user/subscriptions", userController::getMySubscriptions);
        httpServer.addRoute("GET", "/api/user/subscriptions/active", userController::getActiveSubscriptions);
        httpServer.addRoute("POST", "/api/user/subscriptions/{subscriptionId}/cancel", userController::cancelMySubscription);
        
        // Admin Controller routes
        httpServer.addRoute("GET", "/api/admin/dashboard", adminController::getDashboard);
        httpServer.addRoute("POST", "/api/admin/users/{id}/soft-delete", adminController::softDeleteUser);
        httpServer.addRoute("POST", "/api/admin/users/{id}/restore", adminController::restoreUser);
        httpServer.addRoute("GET", "/api/admin/users/deleted", adminController::getDeletedUsers);
        httpServer.addRoute("POST", "/api/admin/devices/{id}/soft-delete", adminController::softDeleteDevice);
        httpServer.addRoute("POST", "/api/admin/devices/{id}/restore", adminController::restoreDevice);
        httpServer.addRoute("GET", "/api/admin/devices/deleted", adminController::getDeletedDevices);
        httpServer.addRoute("POST", "/api/admin/subscriptions/{id}/soft-delete", adminController::softDeleteSubscription);
        httpServer.addRoute("POST", "/api/admin/subscriptions/{id}/restore", adminController::restoreSubscription);
        httpServer.addRoute("GET", "/api/admin/subscriptions/deleted", adminController::getDeletedSubscriptions);
        httpServer.addRoute("POST", "/api/admin/features", adminController::createFeature);
        httpServer.addRoute("GET", "/api/admin/features", adminController::getAllFeatures);
        httpServer.addRoute("GET", "/api/admin/features/active", adminController::getActiveFeatures);
        httpServer.addRoute("POST", "/api/admin/subscriptions/{subscriptionId}/features", adminController::addFeaturesToSubscription);
        httpServer.addRoute("DELETE", "/api/admin/subscriptions/{subscriptionId}/features", adminController::removeFeaturesFromSubscription);
        httpServer.addRoute("POST", "/api/admin/subscriptions", adminController::createSubscription);
        httpServer.addRoute("POST", "/api/admin/user-subscriptions/assign", adminController::assignSubscriptionToUser);
        
        // Agent Controller routes
        httpServer.addRoute("POST", "/api/agent/devices", agentController::createDevice);
        httpServer.addRoute("GET", "/api/agent/devices/{id}/api-key", agentController::getDeviceApiKey);
        httpServer.addRoute("POST", "/api/agent/devices/{id}/regenerate-api-key", agentController::regenerateApiKey);
        httpServer.addRoute("GET", "/api/agent/devices", agentController::getAllDevices);
        httpServer.addRoute("GET", "/api/agent/devices/active", agentController::getActiveDevices);
        httpServer.addRoute("GET", "/api/agent/devices/{id}", agentController::getDeviceById);
        httpServer.addRoute("PUT", "/api/agent/devices/{id}", agentController::updateDevice);
        httpServer.addRoute("DELETE", "/api/agent/devices/{id}", agentController::deactivateDevice);
        httpServer.addRoute("GET", "/api/agent/subscriptions", agentController::getAllSubscriptions);
        httpServer.addRoute("GET", "/api/agent/subscriptions/device/{deviceId}", agentController::getSubscriptionsByDevice);
        httpServer.addRoute("GET", "/api/agent/subscriptions/{id}", agentController::getSubscriptionById);
        httpServer.addRoute("POST", "/api/agent/user-subscriptions/assign", agentController::assignSubscriptionToUser);
        httpServer.addRoute("GET", "/api/agent/user-subscriptions", agentController::getAllUserSubscriptions);
        httpServer.addRoute("GET", "/api/agent/user-subscriptions/active", agentController::getActiveUserSubscriptions);
        httpServer.addRoute("GET", "/api/agent/user-subscriptions/user/{userId}", agentController::getUserSubscriptionsByUser);
        httpServer.addRoute("PUT", "/api/agent/user-subscriptions/{id}/negotiated-price", agentController::updateNegotiatedPrice);
        httpServer.addRoute("POST", "/api/agent/user-subscriptions/{id}/cancel", agentController::cancelSubscription);
        httpServer.addRoute("POST", "/api/agent/user-devices/assign", agentController::assignDeviceToUser);
        httpServer.addRoute("GET", "/api/agent/user-devices", agentController::getAllUserDevices);
        httpServer.addRoute("GET", "/api/agent/user-devices/user/{userId}", agentController::getUserDevicesByUser);
        httpServer.addRoute("GET", "/api/agent/dashboard", agentController::getDashboard);
        
        // Billing Controller routes
        httpServer.addRoute("POST", "/api/billing/generate-monthly", billingController::generateMonthlyBills);
        httpServer.addRoute("POST", "/api/billing/generate/{userSubscriptionId}", billingController::generateBill);
        httpServer.addRoute("GET", "/api/billing/pending", billingController::getPendingBills);
        httpServer.addRoute("GET", "/api/billing/user-subscription/{userSubscriptionId}", billingController::getBillsBySubscription);
        httpServer.addRoute("PUT", "/api/billing/{billingId}/mark-paid", billingController::markAsPaid);
        httpServer.addRoute("PUT", "/api/billing/{billingId}/pay", billingController::payBill);
        httpServer.addRoute("POST", "/api/billing/mark-overdue", billingController::markOverdue);
        
        // Device Verification Controller routes
        httpServer.addRoute("POST", "/api/device/verify-subscription", deviceVerificationController::verifySubscription);
        httpServer.addRoute("GET", "/api/device/health", deviceVerificationController::deviceHealth);
        httpServer.addRoute("GET", "/api/device/info", deviceVerificationController::deviceInfo);
        
        // Audit Log Controller routes
        httpServer.addRoute("GET", "/api/audit", auditLogController::getAllAuditLogs);
        httpServer.addRoute("GET", "/api/audit/trail/{entityType}/{entityId}", auditLogController::getAuditTrail);
        httpServer.addRoute("GET", "/api/audit/user/{userId}", auditLogController::getAuditLogsByUser);
        httpServer.addRoute("GET", "/api/audit/action/{action}", auditLogController::getAuditLogsByAction);
        httpServer.addRoute("GET", "/api/audit/entity/{entityType}", auditLogController::getAuditLogsByEntityType);
        httpServer.addRoute("GET", "/api/audit/date-range", auditLogController::getAuditLogsByDateRange);
        httpServer.addRoute("GET", "/api/audit/failed", auditLogController::getFailedAuditLogs);
        httpServer.addRoute("GET", "/api/audit/search", auditLogController::searchAuditLogs);
        httpServer.addRoute("GET", "/api/audit/statistics", auditLogController::getStatistics);
        
        // Migration Controller routes
        httpServer.addRoute("POST", "/api/admin/migration/users", migrationController::importUser);
        httpServer.addRoute("POST", "/api/admin/migration/devices", migrationController::importDevice);
        httpServer.addRoute("POST", "/api/admin/migration/features", migrationController::importFeature);
        httpServer.addRoute("POST", "/api/admin/migration/subscriptions", migrationController::importSubscription);
        httpServer.addRoute("POST", "/api/admin/migration/user-subscriptions", migrationController::importUserSubscription);
        httpServer.addRoute("POST", "/api/admin/migration/user-devices", migrationController::importUserDevice);
        httpServer.addRoute("POST", "/api/admin/migration/bulk/users", migrationController::bulkImportUsers);
        
        logger.info("REST routes registered successfully - Total: 75 endpoints");
    }
    
    private HttpResponse healthCheck(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = healthCheckController.basicHealth(request);
            recordMetrics("GET", "/health", System.currentTimeMillis() - startTime, response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Error in health check", e);
            recordMetrics("GET", "/health", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse detailedHealthCheck(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = healthCheckController.detailedHealth(request);
            recordMetrics("GET", "/health/detailed", System.currentTimeMillis() - startTime, response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Error in detailed health check", e);
            recordMetrics("GET", "/health/detailed", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse metrics(HttpRequest request) {
        try {
            Map<String, Object> metrics = metricsCollector.getMetrics();
            return HttpResponse.ok(objectMapper.writeValueAsString(metrics));
        } catch (Exception e) {
            logger.error("Error getting metrics", e);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private void recordMetrics(String method, String path, long responseTime, int statusCode) {
        metricsCollector.recordRequest(path, method, responseTime);
        metricsCollector.recordError(path, method, statusCode);
    }
    
    private HttpResponse register(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Registration request received");
            
            // Parse request body
            RegisterRequest registerRequest = objectMapper.readValue(
                request.getBody(), RegisterRequest.class);
            
            // Validate input
            ValidationUtil.validateUsername(registerRequest.getUsername());
            ValidationUtil.validateEmail(registerRequest.getEmail());
            ValidationUtil.validatePassword(registerRequest.getPassword());
            ValidationUtil.validateMobileNumber(registerRequest.getMobileNumber());
            
            // Register user
            UserServicePort userService = container.getBean(UserServicePort.class);
            userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getMobileNumber()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", registerRequest.getUsername());
            
            logger.info("User registered successfully: {}", registerRequest.getUsername());
            HttpResponse httpResponse = HttpResponse.created(objectMapper.writeValueAsString(response));
            recordMetrics("POST", "/api/auth/register", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Registration failed", e);
            recordMetrics("POST", "/api/auth/register", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse login(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Login request received");
            
            // Parse request body
            LoginRequest loginRequest = objectMapper.readValue(
                request.getBody(), LoginRequest.class);
            
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Username is required");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Password is required");
            }
            
            // Authenticate user
            AuthServicePort authService = container.getBean(AuthServicePort.class);
            AuthServicePort.AuthResult authResult = authService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            );
            
            // Build response
            AuthResponse response = new AuthResponse(
                authResult.getAccessToken(),
                authResult.getRefreshToken(),
                authResult.getUser().getUsername(),
                authResult.getUser().getEmail(),
                authResult.getRoles()
            );
            
            logger.info("Login successful: {}", loginRequest.getUsername());
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("POST", "/api/auth/login", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Login failed", e);
            recordMetrics("POST", "/api/auth/login", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse refreshToken(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Token refresh request received");
            
            Map<String, String> body = objectMapper.readValue(
                request.getBody(), Map.class);
            String refreshToken = body.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Refresh token is required");
            }
            
            AuthServicePort authService = container.getBean(AuthServicePort.class);
            AuthServicePort.AuthResult authResult = authService.refreshToken(refreshToken);
            
            AuthResponse response = new AuthResponse(
                authResult.getAccessToken(),
                authResult.getRefreshToken(),
                authResult.getUser().getUsername(),
                authResult.getUser().getEmail(),
                authResult.getRoles()
            );
            
            logger.info("Token refreshed successfully");
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("POST", "/api/auth/refresh", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            recordMetrics("POST", "/api/auth/refresh", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse logout(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Logout request received");
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Authorization header is required");
            }
            
            String accessToken = authHeader.substring(7);
            AuthServicePort authService = container.getBean(AuthServicePort.class);
            authService.logout(accessToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            
            logger.info("Logout successful");
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("POST", "/api/auth/logout", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Logout failed", e);
            recordMetrics("POST", "/api/auth/logout", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse loginByMobile(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Mobile login request received");
            
            Map<String, String> body = objectMapper.readValue(request.getBody(), Map.class);
            String mobileNumber = body.get("mobileNumber");
            String password = body.get("password");
            
            if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Mobile number is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Password is required");
            }
            
            AuthServicePort authService = container.getBean(AuthServicePort.class);
            AuthServicePort.AuthResult authResult = authService.loginByMobile(mobileNumber, password);
            
            AuthResponse response = new AuthResponse(
                authResult.getAccessToken(),
                authResult.getRefreshToken(),
                authResult.getUser().getUsername(),
                authResult.getUser().getEmail(),
                authResult.getRoles()
            );
            
            logger.info("Mobile login successful: {}", mobileNumber);
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("POST", "/api/auth/login/mobile", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Mobile login failed", e);
            recordMetrics("POST", "/api/auth/login/mobile", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse getCurrentUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Get current user request received");
            
            String username = request.getHeader("X-Username");
            if (username == null) {
                throw new com.subscription.subscriptionservice.domain.exception.AuthenticationException("User not authenticated");
            }
            
            AuthServicePort authService = container.getBean(AuthServicePort.class);
            com.subscription.subscriptionservice.domain.model.User user = authService.getCurrentUser(username);
            
            UserResponse response = mapToUserResponse(user);
            
            logger.info("Current user retrieved: {}", username);
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("GET", "/api/auth/me", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Get current user failed", e);
            recordMetrics("GET", "/api/auth/me", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse getUser(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Get user request received");
            
            String idStr = request.getPathParams() != null ? request.getPathParams().get("id") : null;
            if (idStr == null || idStr.trim().isEmpty()) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("User ID is required");
            }
            
            Long id;
            try {
                id = Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                throw new com.subscription.subscriptionservice.domain.exception.ValidationException("Invalid user ID format");
            }
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            com.subscription.subscriptionservice.domain.model.User user = userService.findById(id);
            
            // Convert to response DTO
            UserResponse response = mapToUserResponse(user);
            
            logger.info("User retrieved: userId={}", id);
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("GET", "/api/users/{id}", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Get user failed", e);
            recordMetrics("GET", "/api/users/{id}", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private HttpResponse getAllUsers(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Get all users request received");
            
            // Get query parameter for including deleted users
            String includeDeletedStr = request.getQueryParams() != null ? 
                request.getQueryParams().get("includeDeleted") : null;
            boolean includeDeleted = "true".equalsIgnoreCase(includeDeletedStr);
            
            UserServicePort userService = container.getBean(UserServicePort.class);
            List<com.subscription.subscriptionservice.domain.model.User> users = 
                userService.getAllUsers(includeDeleted);
            
            // Convert to response DTOs
            List<UserResponse> responseList = new ArrayList<>();
            for (com.subscription.subscriptionservice.domain.model.User user : users) {
                responseList.add(mapToUserResponse(user));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", responseList);
            response.put("count", responseList.size());
            
            logger.info("Retrieved {} users", responseList.size());
            HttpResponse httpResponse = HttpResponse.ok(objectMapper.writeValueAsString(response));
            recordMetrics("GET", "/api/users", System.currentTimeMillis() - startTime, httpResponse.getStatusCode());
            return httpResponse;
            
        } catch (Exception e) {
            logger.error("Get all users failed", e);
            recordMetrics("GET", "/api/users", System.currentTimeMillis() - startTime, 500);
            return errorHandler.handleException(e, request.getPath());
        }
    }
    
    private UserResponse mapToUserResponse(com.subscription.subscriptionservice.domain.model.User user) {
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
}
