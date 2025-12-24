package com.subscription.subscriptionservice.infrastructure.util;

import com.framework.core.http.HttpRequest;
import com.subscription.subscriptionservice.domain.exception.ForbiddenException;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for role-based access control
 */
public class RoleChecker {
    
    /**
     * Check if user has any of the required roles
     */
    public static boolean hasAnyRole(HttpRequest request, String... requiredRoles) {
        String rolesHeader = request.getHeaders().get("X-Roles");
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return false;
        }
        
        List<String> userRoles = Arrays.asList(rolesHeader.split(","));
        for (String requiredRole : requiredRoles) {
            if (userRoles.contains(requiredRole)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has all required roles
     */
    public static boolean hasAllRoles(HttpRequest request, String... requiredRoles) {
        String rolesHeader = request.getHeaders().get("X-Roles");
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return false;
        }
        
        List<String> userRoles = Arrays.asList(rolesHeader.split(","));
        for (String requiredRole : requiredRoles) {
            if (!userRoles.contains(requiredRole)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Require user to have any of the specified roles, throw exception if not
     */
    public static void requireAnyRole(HttpRequest request, String... requiredRoles) {
        if (!hasAnyRole(request, requiredRoles)) {
            throw new ForbiddenException("Access denied. Required roles: " + Arrays.toString(requiredRoles));
        }
    }
    
    /**
     * Require user to have all specified roles, throw exception if not
     */
    public static void requireAllRoles(HttpRequest request, String... requiredRoles) {
        if (!hasAllRoles(request, requiredRoles)) {
            throw new ForbiddenException("Access denied. Required roles: " + Arrays.toString(requiredRoles));
        }
    }
}

