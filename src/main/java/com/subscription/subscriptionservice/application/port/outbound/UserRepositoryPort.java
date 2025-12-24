package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Port for user persistence operations
 * This is an outbound port (driven by infrastructure)
 */
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);
    List<User> findAll(boolean includeDeleted);
    List<User> findDeleted();
    void delete(Long id);
}

