package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.Device;

import java.util.List;

public interface DeviceServicePort {
    Device createDevice(String name, String description, String deviceType);
    Device findById(Long id);
    Device findByApiKey(String apiKey);
    List<Device> findAll();
    List<Device> findActive();
    List<Device> findDeleted();
    Device updateDevice(Long id, String name, String description, String deviceType);
    void deleteDevice(Long id);
    void softDeleteDevice(Long id, Long deletedBy);
    void restoreDevice(Long id);
    String regenerateApiKey(Long id);
    String getApiKey(Long id);
}

