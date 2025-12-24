package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.SubscriptionServicePort;
import com.subscription.subscriptionservice.application.port.outbound.DeviceRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.FeatureRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.SubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class SubscriptionUseCase implements SubscriptionServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionUseCase.class);
    
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final DeviceRepositoryPort deviceRepository;
    private final FeatureRepositoryPort featureRepository;
    
    public SubscriptionUseCase(SubscriptionRepositoryPort subscriptionRepository,
                              DeviceRepositoryPort deviceRepository,
                              FeatureRepositoryPort featureRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.deviceRepository = deviceRepository;
        this.featureRepository = featureRepository;
    }
    
    @Override
    public Subscription createSubscription(String name, String description, Long deviceId, BigDecimal basePrice,
                                         Subscription.SubscriptionLevel level, Subscription.BillingCycle billingCycle,
                                         List<Long> featureIds) {
        logger.info("Creating subscription: name={}, deviceId={}", name, deviceId);
        
        // Validate device exists
        deviceRepository.findById(deviceId)
            .orElseThrow(() -> new UserNotFoundException("Device not found with id: " + deviceId));
        
        // Validate features exist
        if (featureIds != null) {
            for (Long featureId : featureIds) {
                featureRepository.findById(featureId)
                    .orElseThrow(() -> new UserNotFoundException("Feature not found with id: " + featureId));
            }
        }
        
        Subscription subscription = new Subscription();
        subscription.setName(name);
        subscription.setDescription(description);
        subscription.setDeviceId(deviceId);
        subscription.setBasePrice(basePrice);
        subscription.setLevel(level);
        subscription.setBillingCycle(billingCycle);
        subscription.setActive(true);
        subscription.setFeatureIds(featureIds != null ? featureIds : List.of());
        
        return subscriptionRepository.save(subscription);
    }
    
    @Override
    public Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Subscription not found with id: " + id));
    }
    
    @Override
    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }
    
    @Override
    public List<Subscription> findActive() {
        return subscriptionRepository.findActive();
    }
    
    @Override
    public List<Subscription> findByDeviceId(Long deviceId) {
        return subscriptionRepository.findByDeviceId(deviceId);
    }
    
    @Override
    public List<Subscription> findDeleted() {
        return subscriptionRepository.findDeleted();
    }
    
    @Override
    public Subscription updateSubscription(Long id, String name, String description, BigDecimal basePrice,
                                         Subscription.SubscriptionLevel level, Subscription.BillingCycle billingCycle) {
        Subscription subscription = findById(id);
        subscription.setName(name);
        subscription.setDescription(description);
        subscription.setBasePrice(basePrice);
        subscription.setLevel(level);
        subscription.setBillingCycle(billingCycle);
        return subscriptionRepository.save(subscription);
    }
    
    @Override
    public void deleteSubscription(Long id) {
        subscriptionRepository.delete(id);
    }
    
    @Override
    public void softDeleteSubscription(Long id, Long deletedBy) {
        subscriptionRepository.softDelete(id, deletedBy);
    }
    
    @Override
    public void restoreSubscription(Long id) {
        subscriptionRepository.restore(id);
    }
    
    @Override
    public void addFeatures(Long subscriptionId, List<Long> featureIds) {
        Subscription subscription = findById(subscriptionId);
        for (Long featureId : featureIds) {
            featureRepository.findById(featureId)
                .orElseThrow(() -> new UserNotFoundException("Feature not found with id: " + featureId));
            subscriptionRepository.addFeature(subscriptionId, featureId);
        }
    }
    
    @Override
    public void removeFeatures(Long subscriptionId, List<Long> featureIds) {
        findById(subscriptionId); // Validate subscription exists
        for (Long featureId : featureIds) {
            subscriptionRepository.removeFeature(subscriptionId, featureId);
        }
    }
}

