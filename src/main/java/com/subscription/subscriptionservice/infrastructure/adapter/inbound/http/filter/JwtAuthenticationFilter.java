package com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.filter;

import com.framework.core.http.Filter;
import com.framework.core.http.FilterChain;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.subscriptionservice.application.port.outbound.SecurityPort;
import com.subscription.subscriptionservice.domain.exception.AuthenticationException;
import com.subscription.subscriptionservice.infrastructure.adapter.inbound.http.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JWT Authentication Filter
 * Protects routes that require authentication
 */
public class JwtAuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final SecurityPort securityPort;
    private final ObjectMapper objectMapper;
    private final Set<String> publicPaths;
    
    public JwtAuthenticationFilter(SecurityPort securityPort, ObjectMapper objectMapper) {
        this.securityPort = securityPort;
        this.objectMapper = objectMapper;
        this.publicPaths = new HashSet<>(Arrays.asList(
            "/health",
            "/health/detailed",
            "/metrics",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/login/mobile",
            "/api/auth/refresh",
            "/api/device/verify-subscription",
            "/api/device/health",
            "/api/device/info"
        ));
    }
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception {
        String path = request.getPath();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            sendUnauthorizedResponse(response, "Authorization header is required");
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Validate token
            if (!securityPort.validateToken(token)) {
                logger.warn("Invalid or expired token for path: {}", path);
                sendUnauthorizedResponse(response, "Invalid or expired token");
                return;
            }
            
            // Extract username and roles from token
            String username = securityPort.getUsernameFromToken(token);
            List<String> roles = securityPort.getRolesFromToken(token);
            
            // Store username and roles in request for use in handlers
            request.getHeaders().put("X-Username", username);
            request.getHeaders().put("X-Roles", String.join(",", roles));
            
            logger.debug("Authenticated user: {} with roles: {} for path: {}", username, roles, path);
            
            // Continue to next filter or handler
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Error validating token", e);
            sendUnauthorizedResponse(response, "Token validation failed");
        }
    }
    
    private boolean isPublicPath(String path) {
        // Exact match
        if (publicPaths.contains(path)) {
            return true;
        }
        
        // Check if path starts with any public path
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void sendUnauthorizedResponse(HttpResponse response, String message) {
        try {
            response.setStatusCode(401);
            response.setHeader("Content-Type", "application/json");
            
            ErrorResponse errorResponse = new ErrorResponse(
                401,
                "AUTHENTICATION_ERROR",
                message,
                ""
            );
            
            response.setBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.error("Error creating error response", e);
            response.setStatusCode(401);
            response.setBody("{\"error\":\"Unauthorized\"}");
        }
    }
}

