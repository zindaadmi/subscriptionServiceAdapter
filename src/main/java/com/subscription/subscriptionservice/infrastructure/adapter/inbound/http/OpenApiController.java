package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http;

import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAPI/Swagger Documentation Controller
 * Provides API documentation in OpenAPI 3.0 format
 */
public class OpenApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenApiController.class);
    private final ObjectMapper objectMapper;
    
    public OpenApiController() {
        this.objectMapper = new ObjectMapper();
    }
    
    public HttpResponse getOpenApiSpec(HttpRequest request) {
        try {
            Map<String, Object> openApi = new HashMap<>();
            openApi.put("openapi", "3.0.0");
            
            Map<String, Object> info = new HashMap<>();
            info.put("title", "Subscription Service API");
            info.put("version", "1.0.0");
            info.put("description", "Complete REST API for Subscription Management Service");
            openApi.put("info", info);
            
            Map<String, Object> servers = new HashMap<>();
            servers.put("url", "http://localhost:8080");
            servers.put("description", "Development server");
            openApi.put("servers", new Object[]{servers});
            
            // Note: Full OpenAPI spec would include all paths, schemas, etc.
            // This is a basic structure - can be expanded with full API documentation
            Map<String, Object> paths = new HashMap<>();
            openApi.put("paths", paths);
            
            // Add basic paths structure
            addPath(paths, "/health", "GET", "Basic health check");
            addPath(paths, "/api/auth/register", "POST", "Register new user");
            addPath(paths, "/api/auth/login", "POST", "User login");
            
            return HttpResponse.ok(objectMapper.writeValueAsString(openApi));
        } catch (Exception e) {
            logger.error("Error generating OpenAPI spec", e);
            return HttpResponse.serverError("{\"error\":\"Failed to generate API documentation\"}");
        }
    }
    
    private void addPath(Map<String, Object> paths, String path, String method, String summary) {
        if (!paths.containsKey(path)) {
            paths.put(path, new HashMap<>());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> pathMap = (Map<String, Object>) paths.get(path);
        
        Map<String, Object> operation = new HashMap<>();
        operation.put("summary", summary);
        operation.put("operationId", method.toLowerCase() + path.replace("/", "_").replace("{", "").replace("}", ""));
        
        pathMap.put(method.toLowerCase(), operation);
    }
}

