package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.TestBase;
import com.subscription.subscriptionservice.application.port.inbound.UserServicePort;
import com.subscription.subscriptionservice.domain.exception.DuplicateEntityException;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserUseCase
 */
@DisplayName("UserUseCase Tests")
public class UserUseCaseTest extends TestBase {
    
    @Test
    @DisplayName("Should register a new user successfully")
    public void testRegisterUser() {
        UserServicePort userService = container.getBean(UserServicePort.class);
        
        User user = userService.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "+1234567890"
        );
        
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
    }
    
    @Test
    @DisplayName("Should throw exception when registering duplicate username")
    public void testRegisterDuplicateUsername() {
        UserServicePort userService = container.getBean(UserServicePort.class);
        
        userService.registerUser("testuser", "test1@example.com", "password123", "+1111111111");
        
        assertThrows(DuplicateEntityException.class, () -> {
            userService.registerUser("testuser", "test2@example.com", "password123", "+2222222222");
        });
    }
    
    @Test
    @DisplayName("Should find user by username")
    public void testFindByUsername() {
        UserServicePort userService = container.getBean(UserServicePort.class);
        
        userService.registerUser("testuser", "test@example.com", "password123", "+1234567890");
        
        User found = userService.findByUsername("testuser");
        
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found")
    public void testFindByUsernameNotFound() {
        UserServicePort userService = container.getBean(UserServicePort.class);
        
        assertThrows(UserNotFoundException.class, () -> {
            userService.findByUsername("nonexistent");
        });
    }
}

