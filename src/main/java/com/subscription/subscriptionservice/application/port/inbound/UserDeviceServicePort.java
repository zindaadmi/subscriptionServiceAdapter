package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.UserDevice;

import java.util.List;

public interface UserDeviceServicePort {
    UserDevice assignDevice(Long userId, Long deviceId, Long subscriptionId, Long userSubscriptionId, String deviceSerial);
    UserDevice findById(Long id);
    UserDevice findByDeviceSerial(String deviceSerial);
    List<UserDevice> findAll();
    List<UserDevice> findByUserId(Long userId);
    List<UserDevice> findByDeviceId(Long deviceId);
    List<UserDevice> findByUserSubscriptionId(Long userSubscriptionId);
    void deactivateDevice(Long id);
    void deleteDevice(Long id);
}

