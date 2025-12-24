package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.UserServicePort;
import com.subscription.subscriptionservice.application.port.outbound.CachePort;
import com.subscription.subscriptionservice.application.port.outbound.SecurityPort;
import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.DuplicateEntityException;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.User;
import com.subscription.subscriptionservice.infrastructure.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use case implementation for user management
 * Depends only on ports (interfaces), not on infrastructure
 */
public class UserUseCase implements UserServicePort {

    private static final Logger logger = LoggerFactory.getLogger(UserUseCase.class);
    
    private final UserRepositoryPort userRepository;
    private final SecurityPort securityPort;
    private final TransactionManager transactionManager;
    private CachePort cachePort; // Optional - can be null

    public UserUseCase(UserRepositoryPort userRepository, SecurityPort securityPort, 
                       TransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.securityPort = securityPort;
        this.transactionManager = transactionManager;
        this.cachePort = null; // Will be set if cache adapter is registered
    }
    
    // Set cache port if available (called by container)
    public void setCachePort(CachePort cachePort) {
        this.cachePort = cachePort;
    }

    @Override
    public User registerUser(String username, String email, String password, String mobileNumber) {
        logger.info("Registering new user: username={}", username);
        
        return transactionManager.executeInTransaction(() -> {
            // Check if user already exists
            if (userRepository.findByUsername(username).isPresent()) {
                logger.warn("Registration failed: Username already exists - {}", username);
                throw new DuplicateEntityException("Username already exists");
            }
            if (email != null && userRepository.findByEmail(email).isPresent()) {
                logger.warn("Registration failed: Email already exists - {}", email);
                throw new DuplicateEntityException("Email already exists");
            }
            if (mobileNumber != null && userRepository.findByMobileNumber(mobileNumber).isPresent()) {
                logger.warn("Registration failed: Mobile number already exists - {}", mobileNumber);
                throw new DuplicateEntityException("Mobile number already exists");
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setMobileNumber(mobileNumber);
            user.setPassword(securityPort.hashPassword(password));
            user.setEnabled(true);
            user.setDeleted(false);
            user.setProvider(User.AuthProvider.LOCAL);

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: userId={}, username={}", savedUser.getId(), username);
            
            // Invalidate cache
            if (cachePort != null) {
                cachePort.delete(CacheUtil.buildKey("user", savedUser.getId()));
                cachePort.delete(CacheUtil.buildKey("user:username", username));
            }
            
            return savedUser;
        });
    }

    @Override
    public User registerGoogleUser(String email, String name, String providerId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setUsername(email); // Use email as username for OAuth users
        user.setEmail(email);
        user.setProvider(User.AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setEnabled(true);
        user.setDeleted(false);

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        // Try cache first
        String cacheKey = CacheUtil.buildKey("user:username", username);
        User cachedUser = CacheUtil.get(cachePort, cacheKey, User.class);
        if (cachedUser != null) {
            logger.debug("User found in cache: username={}", username);
            return cachedUser;
        }
        
        // Cache miss - get from repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        // Cache for 5 minutes
        CacheUtil.put(cachePort, cacheKey, user, 300);
        // Also cache by ID
        if (user.getId() != null) {
            CacheUtil.put(cachePort, CacheUtil.buildKey("user", user.getId()), user, 300);
        }
        logger.debug("User cached: username={}", username);
        
        return user;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public User findByMobileNumber(String mobileNumber) {
        return userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with mobile: " + mobileNumber));
    }

    @Override
    public User findById(Long id) {
        // Try cache first
        String cacheKey = CacheUtil.buildKey("user", id);
        User cachedUser = CacheUtil.get(cachePort, cacheKey, User.class);
        if (cachedUser != null) {
            logger.debug("User found in cache: id={}", id);
            return cachedUser;
        }
        
        // Cache miss - get from repository
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        
        // Cache for 5 minutes
        CacheUtil.put(cachePort, cacheKey, user, 300);
        logger.debug("User cached: id={}", id);
        
        return user;
    }

    @Override
    public User softDeleteUser(Long userId, Long deletedBy) {
        User user = findById(userId);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    @Override
    public User restoreUser(Long userId) {
        User user = findById(userId);
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers(boolean includeDeleted) {
        return userRepository.findAll(includeDeleted);
    }

    @Override
    public List<User> getDeletedUsers() {
        return userRepository.findDeleted();
    }

    @Override
    public User updateUserProfile(Long userId, String email, String phoneNumber, String address,
                                  String city, String state, String zipCode, String country) {
        User user = findById(userId);
        if (email != null) user.setEmail(email);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (address != null) user.setAddress(address);
        if (city != null) user.setCity(city);
        if (state != null) user.setState(state);
        if (zipCode != null) user.setZipCode(zipCode);
        if (country != null) user.setCountry(country);
        return userRepository.save(user);
    }
}

