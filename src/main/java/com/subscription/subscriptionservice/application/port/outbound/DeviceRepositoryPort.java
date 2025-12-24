package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findById(Long id);
    Optional<Device> findByApiKey(String apiKey);
    List<Device> findAll();
    List<Device> findActive();
    List<Device> findDeleted();
    void delete(Long id);
    void softDelete(Long id, Long deletedBy);
    void restore(Long id);
    String generateApiKey();
}

