package com.subscription.subscriptionservice.infrastructure.util;

import com.subscription.subscriptionservice.domain.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("username", "Username is required");
        }
        if (username.length() < 3) {
            throw new ValidationException("username", "Username must be at least 3 characters");
        }
        if (username.length() > 50) {
            throw new ValidationException("username", "Username must not exceed 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("username", "Username can only contain letters, numbers, and underscores");
        }
    }
    
    public static void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ValidationException("email", "Invalid email format");
            }
        }
    }
    
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("password", "Password is required");
        }
        if (password.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters");
        }
        if (password.length() > 100) {
            throw new ValidationException("password", "Password must not exceed 100 characters");
        }
    }
    
    public static void validateMobileNumber(String mobileNumber) {
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            if (!MOBILE_PATTERN.matcher(mobileNumber).matches()) {
                throw new ValidationException("mobileNumber", "Invalid mobile number format");
            }
        }
    }
}

