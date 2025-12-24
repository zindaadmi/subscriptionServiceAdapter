package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.DeviceServicePort;
import com.subscription.subscriptionservice.application.port.outbound.DeviceRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeviceUseCase implements DeviceServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceUseCase.class);
    
    private final DeviceRepositoryPort deviceRepository;
    
    public DeviceUseCase(DeviceRepositoryPort deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    @Override
    public Device createDevice(String name, String description, String deviceType) {
        logger.info("Creating device: name={}, type={}", name, deviceType);
        Device device = new Device();
        device.setName(name);
        device.setDescription(description);
        device.setDeviceType(deviceType);
        device.setActive(true);
        device.setApiKey(deviceRepository.generateApiKey());
        return deviceRepository.save(device);
    }
    
    @Override
    public Device findById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Device not found with id: " + id));
    }
    
    @Override
    public Device findByApiKey(String apiKey) {
        return deviceRepository.findByApiKey(apiKey)
            .orElseThrow(() -> new UserNotFoundException("Device not found with api key: " + apiKey));
    }
    
    @Override
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }
    
    @Override
    public List<Device> findActive() {
        return deviceRepository.findActive();
    }
    
    @Override
    public List<Device> findDeleted() {
        return deviceRepository.findDeleted();
    }
    
    @Override
    public Device updateDevice(Long id, String name, String description, String deviceType) {
        Device device = findById(id);
        device.setName(name);
        device.setDescription(description);
        device.setDeviceType(deviceType);
        return deviceRepository.save(device);
    }
    
    @Override
    public void deleteDevice(Long id) {
        deviceRepository.delete(id);
    }
    
    @Override
    public void softDeleteDevice(Long id, Long deletedBy) {
        deviceRepository.softDelete(id, deletedBy);
    }
    
    @Override
    public void restoreDevice(Long id) {
        deviceRepository.restore(id);
    }
    
    @Override
    public String regenerateApiKey(Long id) {
        Device device = findById(id);
        String newApiKey = deviceRepository.generateApiKey();
        device.setApiKey(newApiKey);
        deviceRepository.save(device);
        return newApiKey;
    }
    
    @Override
    public String getApiKey(Long id) {
        Device device = findById(id);
        return device.getApiKey();
    }
}

