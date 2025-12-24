package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.User;

/**
 * Port for security operations (password hashing, JWT, etc.)
 */
public interface SecurityPort {
    String hashPassword(String plainPassword);
    boolean verifyPassword(String plainPassword, String hashedPassword);
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String getUsernameFromToken(String token);
    List<String> getRolesFromToken(String token);
    boolean validateToken(String token);
    boolean isTokenExpired(String token);
    void blacklistToken(String token, long expirationTimeMillis);
    boolean isTokenBlacklisted(String token);
    long getTokenExpirationTime(String token);
}

