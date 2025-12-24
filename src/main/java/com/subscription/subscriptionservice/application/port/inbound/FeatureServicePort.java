package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.Feature;

import java.util.List;

public interface FeatureServicePort {
    Feature createFeature(String name, String description, String featureCode);
    Feature findById(Long id);
    Feature findByName(String name);
    Feature findByFeatureCode(String featureCode);
    List<Feature> findAll();
    List<Feature> findActive();
    void deleteFeature(Long id);
}

