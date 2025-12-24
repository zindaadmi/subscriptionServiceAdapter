package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.NewFeatureServicePort;
import com.subscription.subscriptionservice.application.port.outbound.NewEntityRepositoryPort;
import com.subscription.subscriptionservice.domain.model.NewEntity;
import com.subscription.subscriptionservice.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template for creating a new use case (business logic).
 * 
 * Steps to use:
 * 1. Replace "NewFeature" with your feature name
 * 2. Replace "NewEntity" with your domain model
 * 3. Implement your business logic
 * 4. Register in application.yml under "services"
 */
public class NewFeatureUseCase implements NewFeatureServicePort {

    private static final Logger logger = LoggerFactory.getLogger(NewFeatureUseCase.class);
    
    private final NewEntityRepositoryPort repository;

    public NewFeatureUseCase(NewEntityRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Example business logic method
     */
    @Override
    public NewEntity doSomething(Long id) {
        // 1. Validate input
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid ID");
        }

        // 2. Business logic
        NewEntity entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));

        // 3. Apply business rules
        // ... your business logic here ...

        // 4. Save/update
        return repository.save(entity);
    }

    /**
     * Example: Create new entity
     */
    @Override
    public NewEntity create(NewEntity entity) {
        // 1. Validate
        if (entity == null) {
            throw new ValidationException("Entity cannot be null");
        }

        // 2. Business rules
        // ... your business logic here ...

        // 3. Save
        return repository.save(entity);
    }
}

