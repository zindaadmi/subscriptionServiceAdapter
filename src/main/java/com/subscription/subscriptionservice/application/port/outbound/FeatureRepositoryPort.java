package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.Feature;

import java.util.List;
import java.util.Optional;

public interface FeatureRepositoryPort {
    Feature save(Feature feature);
    Optional<Feature> findById(Long id);
    Optional<Feature> findByName(String name);
    Optional<Feature> findByFeatureCode(String featureCode);
    List<Feature> findAll();
    List<Feature> findActive();
    void delete(Long id);
}

