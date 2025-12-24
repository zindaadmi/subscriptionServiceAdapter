package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepositoryPort {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(Long id);
    List<Subscription> findAll();
    List<Subscription> findActive();
    List<Subscription> findByDeviceId(Long deviceId);
    List<Subscription> findDeleted();
    void delete(Long id);
    void softDelete(Long id, Long deletedBy);
    void restore(Long id);
    void addFeature(Long subscriptionId, Long featureId);
    void removeFeature(Long subscriptionId, Long featureId);
    List<Long> findFeatureIds(Long subscriptionId);
}

