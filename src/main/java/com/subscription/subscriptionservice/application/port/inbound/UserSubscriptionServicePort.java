package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.UserSubscription;

import java.math.BigDecimal;
import java.util.List;

public interface UserSubscriptionServicePort {
    UserSubscription assignSubscription(Long userId, Long subscriptionId, BigDecimal negotiatedPrice,
                                       Integer durationMonths, Long assignedBy);
    UserSubscription findById(Long id);
    List<UserSubscription> findAll();
    List<UserSubscription> findByUserId(Long userId);
    List<UserSubscription> findByUserIdAndStatus(Long userId, UserSubscription.SubscriptionStatus status);
    List<UserSubscription> findActive();
    List<UserSubscription> findBySubscriptionId(Long subscriptionId);
    UserSubscription updateNegotiatedPrice(Long id, BigDecimal negotiatedPrice);
    void cancelSubscription(Long id);
    void cancelUserSubscription(Long userId, Long subscriptionId);
}

