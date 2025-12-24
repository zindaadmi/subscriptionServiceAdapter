package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.FeatureServicePort;
import com.subscription.subscriptionservice.application.port.outbound.FeatureRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.DuplicateEntityException;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FeatureUseCase implements FeatureServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureUseCase.class);
    
    private final FeatureRepositoryPort featureRepository;
    
    public FeatureUseCase(FeatureRepositoryPort featureRepository) {
        this.featureRepository = featureRepository;
    }
    
    @Override
    public Feature createFeature(String name, String description, String featureCode) {
        logger.info("Creating feature: name={}, code={}", name, featureCode);
        
        if (featureRepository.findByName(name).isPresent()) {
            throw new DuplicateEntityException("Feature with name already exists: " + name);
        }
        if (featureCode != null && featureRepository.findByFeatureCode(featureCode).isPresent()) {
            throw new DuplicateEntityException("Feature with code already exists: " + featureCode);
        }
        
        Feature feature = new Feature();
        feature.setName(name);
        feature.setDescription(description);
        feature.setFeatureCode(featureCode);
        feature.setActive(true);
        return featureRepository.save(feature);
    }
    
    @Override
    public Feature findById(Long id) {
        return featureRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Feature not found with id: " + id));
    }
    
    @Override
    public Feature findByName(String name) {
        return featureRepository.findByName(name)
            .orElseThrow(() -> new UserNotFoundException("Feature not found with name: " + name));
    }
    
    @Override
    public Feature findByFeatureCode(String featureCode) {
        return featureRepository.findByFeatureCode(featureCode)
            .orElseThrow(() -> new UserNotFoundException("Feature not found with code: " + featureCode));
    }
    
    @Override
    public List<Feature> findAll() {
        return featureRepository.findAll();
    }
    
    @Override
    public List<Feature> findActive() {
        return featureRepository.findActive();
    }
    
    @Override
    public void deleteFeature(Long id) {
        featureRepository.delete(id);
    }
}

