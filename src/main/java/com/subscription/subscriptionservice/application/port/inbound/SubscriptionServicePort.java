package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.Subscription;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionServicePort {
    Subscription createSubscription(String name, String description, Long deviceId, BigDecimal basePrice,
                                   Subscription.SubscriptionLevel level, Subscription.BillingCycle billingCycle,
                                   List<Long> featureIds);
    Subscription findById(Long id);
    List<Subscription> findAll();
    List<Subscription> findActive();
    List<Subscription> findByDeviceId(Long deviceId);
    List<Subscription> findDeleted();
    Subscription updateSubscription(Long id, String name, String description, BigDecimal basePrice,
                                   Subscription.SubscriptionLevel level, Subscription.BillingCycle billingCycle);
    void deleteSubscription(Long id);
    void softDeleteSubscription(Long id, Long deletedBy);
    void restoreSubscription(Long id);
    void addFeatures(Long subscriptionId, List<Long> featureIds);
    void removeFeatures(Long subscriptionId, List<Long> featureIds);
}

