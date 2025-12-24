package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.subscription.subscriptionservice.application.port.inbound.NewFeatureServicePort;
import com.subscription.subscriptionservice.infrastructure.util.RoleChecker;
import com.subscription.subscriptionservice.domain.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template for creating a new controller.
 * 
 * Steps to use:
 * 1. Replace "NewFeature" with your feature name
 * 2. Replace "NewFeatureServicePort" with your service port
 * 3. Add your methods
 * 4. Register routes in RestControllerAdapter.java
 */
public class NewFeatureController {

    private static final Logger logger = LoggerFactory.getLogger(NewFeatureController.class);
    
    private final Container container;
    private final ObjectMapper objectMapper;

    public NewFeatureController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Example: GET endpoint
     * 
     * To add role check, use:
     * RoleChecker.requireRole(request, "ROLE_ADMIN");
     */
    public HttpResponse getSomething(HttpRequest request) {
        try {
            // Optional: Add role check
            // RoleChecker.requireRole(request, "ROLE_ADMIN");
            
            NewFeatureServicePort service = container.getBean(NewFeatureServicePort.class);
            
            // Call service
            // Object result = service.doSomething();
            
            // Return response
            return HttpResponse.ok(objectMapper.writeValueAsString(/* result */));
            
        } catch (ForbiddenException e) {
            return HttpResponse.forbidden(objectMapper.writeValueAsString(
                Map.of("error", "Forbidden", "message", e.getMessage())
            ));
        } catch (Exception e) {
            logger.error("Error in getSomething", e);
            return HttpResponse.serverError(objectMapper.writeValueAsString(
                Map.of("error", "Internal server error", "message", e.getMessage())
            ));
        }
    }

    /**
     * Example: POST endpoint
     */
    public HttpResponse createSomething(HttpRequest request) {
        try {
            // Parse request body
            // YourRequestDto dto = objectMapper.readValue(request.getBody(), YourRequestDto.class);
            
            NewFeatureServicePort service = container.getBean(NewFeatureServicePort.class);
            
            // Call service
            // Object result = service.createSomething(dto);
            
            return HttpResponse.created(objectMapper.writeValueAsString(/* result */));
            
        } catch (Exception e) {
            logger.error("Error in createSomething", e);
            return HttpResponse.serverError(objectMapper.writeValueAsString(
                Map.of("error", "Internal server error", "message", e.getMessage())
            ));
        }
    }
}

