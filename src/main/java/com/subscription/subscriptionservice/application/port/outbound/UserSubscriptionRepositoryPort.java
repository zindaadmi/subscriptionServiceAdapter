package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.UserSubscription;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepositoryPort {
    UserSubscription save(UserSubscription userSubscription);
    Optional<UserSubscription> findById(Long id);
    List<UserSubscription> findAll();
    List<UserSubscription> findByUserId(Long userId);
    List<UserSubscription> findByUserIdAndStatus(Long userId, UserSubscription.SubscriptionStatus status);
    List<UserSubscription> findActive();
    List<UserSubscription> findBySubscriptionId(Long subscriptionId);
    void delete(Long id);
}

