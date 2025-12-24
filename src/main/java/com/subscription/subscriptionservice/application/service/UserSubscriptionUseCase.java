package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.UserSubscriptionServicePort;
import com.subscription.subscriptionservice.application.port.outbound.SubscriptionRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.UserSubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.UserSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UserSubscriptionUseCase implements UserSubscriptionServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSubscriptionUseCase.class);
    
    private final UserSubscriptionRepositoryPort userSubscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final TransactionManager transactionManager;
    
    public UserSubscriptionUseCase(UserSubscriptionRepositoryPort userSubscriptionRepository,
                                  UserRepositoryPort userRepository,
                                  SubscriptionRepositoryPort subscriptionRepository,
                                  TransactionManager transactionManager) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.transactionManager = transactionManager;
    }
    
    @Override
    public UserSubscription assignSubscription(Long userId, Long subscriptionId, BigDecimal negotiatedPrice,
                                             Integer durationMonths, Long assignedBy) {
        logger.info("Assigning subscription: userId={}, subscriptionId={}", userId, subscriptionId);
        
        return transactionManager.executeInTransaction(() -> {
            // Validate user exists
            userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            // Validate subscription exists
            subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new UserNotFoundException("Subscription not found with id: " + subscriptionId));
            
            UserSubscription userSubscription = new UserSubscription();
            userSubscription.setUserId(userId);
            userSubscription.setSubscriptionId(subscriptionId);
            userSubscription.setNegotiatedPrice(negotiatedPrice);
            userSubscription.setStartDate(LocalDate.now());
            userSubscription.setBillingStartDate(LocalDate.now());
            userSubscription.setStatus(UserSubscription.SubscriptionStatus.ACTIVE);
            userSubscription.setDurationMonths(durationMonths != null ? durationMonths : 1);
            userSubscription.setAssignedBy(assignedBy);
            
            if (durationMonths != null && durationMonths > 0) {
                userSubscription.setEndDate(LocalDate.now().plusMonths(durationMonths));
            }
            
            return userSubscriptionRepository.save(userSubscription);
        });
    }
    
    @Override
    public UserSubscription findById(Long id) {
        return userSubscriptionRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User subscription not found with id: " + id));
    }
    
    @Override
    public List<UserSubscription> findAll() {
        return userSubscriptionRepository.findAll();
    }
    
    @Override
    public List<UserSubscription> findByUserId(Long userId) {
        return userSubscriptionRepository.findByUserId(userId);
    }
    
    @Override
    public List<UserSubscription> findByUserIdAndStatus(Long userId, UserSubscription.SubscriptionStatus status) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, status);
    }
    
    @Override
    public List<UserSubscription> findActive() {
        return userSubscriptionRepository.findActive();
    }
    
    @Override
    public List<UserSubscription> findBySubscriptionId(Long subscriptionId) {
        return userSubscriptionRepository.findBySubscriptionId(subscriptionId);
    }
    
    @Override
    public UserSubscription updateNegotiatedPrice(Long id, BigDecimal negotiatedPrice) {
        UserSubscription userSubscription = findById(id);
        userSubscription.setNegotiatedPrice(negotiatedPrice);
        return userSubscriptionRepository.save(userSubscription);
    }
    
    @Override
    public void cancelSubscription(Long id) {
        UserSubscription userSubscription = findById(id);
        userSubscription.cancel();
        userSubscriptionRepository.save(userSubscription);
    }
    
    @Override
    public void cancelUserSubscription(Long userId, Long subscriptionId) {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findByUserId(userId);
        for (UserSubscription us : subscriptions) {
            if (us.getSubscriptionId().equals(subscriptionId) && us.isActive()) {
                cancelSubscription(us.getId());
                return;
            }
        }
        throw new UserNotFoundException("Active subscription not found for user: " + userId + ", subscription: " + subscriptionId);
    }
}

