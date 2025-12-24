package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.UserDeviceServicePort;
import com.subscription.subscriptionservice.application.port.outbound.DeviceRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import com.subscription.subscriptionservice.application.port.outbound.UserDeviceRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.UserSubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.DuplicateEntityException;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.UserDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class UserDeviceUseCase implements UserDeviceServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDeviceUseCase.class);
    
    private final UserDeviceRepositoryPort userDeviceRepository;
    private final UserRepositoryPort userRepository;
    private final DeviceRepositoryPort deviceRepository;
    private final UserSubscriptionRepositoryPort userSubscriptionRepository;
    private final TransactionManager transactionManager;
    
    public UserDeviceUseCase(UserDeviceRepositoryPort userDeviceRepository,
                            UserRepositoryPort userRepository,
                            DeviceRepositoryPort deviceRepository,
                            UserSubscriptionRepositoryPort userSubscriptionRepository,
                            TransactionManager transactionManager) {
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.transactionManager = transactionManager;
    }
    
    @Override
    public UserDevice assignDevice(Long userId, Long deviceId, Long subscriptionId, Long userSubscriptionId, String deviceSerial) {
        logger.info("Assigning device: userId={}, deviceId={}, serial={}", userId, deviceId, deviceSerial);
        
        return transactionManager.executeInTransaction(() -> {
            // Validate user exists
            userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            // Validate device exists
            deviceRepository.findById(deviceId)
                .orElseThrow(() -> new UserNotFoundException("Device not found with id: " + deviceId));
            
            // Validate user subscription exists
            userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new UserNotFoundException("User subscription not found with id: " + userSubscriptionId));
            
            // Check if device serial already exists
            if (deviceSerial != null && userDeviceRepository.findByDeviceSerial(deviceSerial).isPresent()) {
                throw new DuplicateEntityException("Device serial already exists: " + deviceSerial);
            }
            
            UserDevice userDevice = new UserDevice();
            userDevice.setUserId(userId);
            userDevice.setDeviceId(deviceId);
            userDevice.setSubscriptionId(subscriptionId);
            userDevice.setUserSubscriptionId(userSubscriptionId);
            userDevice.setDeviceSerial(deviceSerial);
            userDevice.setPurchaseDate(LocalDate.now());
            userDevice.setActive(true);
            
            return userDeviceRepository.save(userDevice);
        });
    }
    
    @Override
    public UserDevice findById(Long id) {
        return userDeviceRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User device not found with id: " + id));
    }
    
    @Override
    public UserDevice findByDeviceSerial(String deviceSerial) {
        return userDeviceRepository.findByDeviceSerial(deviceSerial)
            .orElseThrow(() -> new UserNotFoundException("User device not found with serial: " + deviceSerial));
    }
    
    @Override
    public List<UserDevice> findAll() {
        return userDeviceRepository.findAll();
    }
    
    @Override
    public List<UserDevice> findByUserId(Long userId) {
        return userDeviceRepository.findByUserId(userId);
    }
    
    @Override
    public List<UserDevice> findByDeviceId(Long deviceId) {
        return userDeviceRepository.findByDeviceId(deviceId);
    }
    
    @Override
    public List<UserDevice> findByUserSubscriptionId(Long userSubscriptionId) {
        return userDeviceRepository.findByUserSubscriptionId(userSubscriptionId);
    }
    
    @Override
    public void deactivateDevice(Long id) {
        UserDevice userDevice = findById(id);
        userDevice.deactivate();
        userDeviceRepository.save(userDevice);
    }
    
    @Override
    public void deleteDevice(Long id) {
        userDeviceRepository.delete(id);
    }
}

