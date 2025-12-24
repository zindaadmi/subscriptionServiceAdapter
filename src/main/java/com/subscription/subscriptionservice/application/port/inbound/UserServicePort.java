package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.User;

import java.util.List;

/**
 * Port for user management use cases
 * This is an inbound port (driven by application)
 */
public interface UserServicePort {
    User registerUser(String username, String email, String password, String mobileNumber);
    User registerGoogleUser(String email, String name, String providerId);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByMobileNumber(String mobileNumber);
    User findById(Long id);
    User softDeleteUser(Long userId, Long deletedBy);
    User restoreUser(Long userId);
    List<User> getAllUsers(boolean includeDeleted);
    List<User> getDeletedUsers();
    User updateUserProfile(Long userId, String email, String phoneNumber, String address,
                          String city, String state, String zipCode, String country);
}

