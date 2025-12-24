package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.User;

import java.util.List;

/**
 * Port for authentication use cases
 */
public interface AuthServicePort {
    AuthResult login(String username, String password);
    AuthResult loginByMobile(String mobileNumber, String password);
    AuthResult refreshToken(String refreshToken);
    void logout(String accessToken);
    User getCurrentUser(String username);
    
    class AuthResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        private final List<String> roles;
        
        public AuthResult(String accessToken, String refreshToken, User user, List<String> roles) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
            this.roles = roles;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public User getUser() {
            return user;
        }
        
        public List<String> getRoles() {
            return roles;
        }
    }
}

