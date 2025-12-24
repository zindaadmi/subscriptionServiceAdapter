package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.AuthServicePort;
import com.subscription.subscriptionservice.application.port.outbound.SecurityPort;
import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.AuthenticationException;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication use case implementation
 */
public class AuthUseCase implements AuthServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthUseCase.class);
    
    private final UserRepositoryPort userRepository;
    private final SecurityPort securityPort;
    
    public AuthUseCase(UserRepositoryPort userRepository, SecurityPort securityPort) {
        this.userRepository = userRepository;
        this.securityPort = securityPort;
    }
    
    @Override
    public AuthResult login(String username, String password) {
        logger.info("Attempting login for username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found - {}", username);
                    return new AuthenticationException("Invalid username or password");
                });
        
        if (!user.isActive()) {
            logger.warn("Login failed: User account is disabled - {}", username);
            throw new AuthenticationException("User account is disabled");
        }
        
        if (!securityPort.verifyPassword(password, user.getPassword())) {
            logger.warn("Login failed: Invalid password for user - {}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        
        String accessToken = securityPort.generateAccessToken(user);
        String refreshToken = securityPort.generateRefreshToken(user);
        
        List<String> roles = new ArrayList<>();
        for (String roleName : user.getRoleNames()) {
            roles.add(roleName);
        }
        
        logger.info("Login successful for user: {}", username);
        
        return new AuthResult(accessToken, refreshToken, user, roles);
    }
    
    @Override
    public AuthResult loginByMobile(String mobileNumber, String password) {
        logger.info("Attempting login for mobile: {}", mobileNumber);
        
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found - {}", mobileNumber);
                    return new AuthenticationException("Invalid mobile number or password");
                });
        
        if (!user.isActive()) {
            logger.warn("Login failed: User account is disabled - {}", mobileNumber);
            throw new AuthenticationException("User account is disabled");
        }
        
        if (!securityPort.verifyPassword(password, user.getPassword())) {
            logger.warn("Login failed: Invalid password for mobile - {}", mobileNumber);
            throw new AuthenticationException("Invalid mobile number or password");
        }
        
        String accessToken = securityPort.generateAccessToken(user);
        String refreshToken = securityPort.generateRefreshToken(user);
        
        List<String> roles = new ArrayList<>();
        for (String roleName : user.getRoleNames()) {
            roles.add(roleName);
        }
        
        logger.info("Login successful for mobile: {}", mobileNumber);
        
        return new AuthResult(accessToken, refreshToken, user, roles);
    }
    
    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
    
    @Override
    public AuthResult refreshToken(String refreshToken) {
        logger.info("Refreshing token");
        
        if (securityPort.isTokenBlacklisted(refreshToken)) {
            logger.warn("Token refresh failed: Token is blacklisted");
            throw new AuthenticationException("Token is invalid");
        }
        
        if (securityPort.isTokenExpired(refreshToken)) {
            logger.warn("Token refresh failed: Token is expired");
            throw new AuthenticationException("Token is expired");
        }
        
        String username = securityPort.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: User not found - {}", username);
                    return new UserNotFoundException("User not found");
                });
        
        // Blacklist old refresh token (token rotation)
        securityPort.blacklistToken(refreshToken, 
            System.currentTimeMillis() + 604800000); // 7 days
        
        String newAccessToken = securityPort.generateAccessToken(user);
        String newRefreshToken = securityPort.generateRefreshToken(user);
        
        List<String> roles = new ArrayList<>();
        for (String roleName : user.getRoleNames()) {
            roles.add(roleName);
        }
        
        logger.info("Token refreshed successfully for user: {}", username);
        
        return new AuthResult(newAccessToken, newRefreshToken, user, roles);
    }
    
    @Override
    public void logout(String accessToken) {
        logger.info("Logging out user");
        
        if (securityPort.isTokenExpired(accessToken)) {
            return; // Already expired, no need to blacklist
        }
        
        // Get expiration time from token and blacklist
        long expirationTime = securityPort.getTokenExpirationTime(accessToken);
        securityPort.blacklistToken(accessToken, expirationTime);
        
        logger.info("User logged out successfully");
    }
}

