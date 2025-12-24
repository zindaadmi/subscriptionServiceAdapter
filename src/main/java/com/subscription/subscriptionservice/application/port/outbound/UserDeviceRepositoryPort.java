package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.UserDevice;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepositoryPort {
    UserDevice save(UserDevice userDevice);
    Optional<UserDevice> findById(Long id);
    Optional<UserDevice> findByDeviceSerial(String deviceSerial);
    List<UserDevice> findAll();
    List<UserDevice> findByUserId(Long userId);
    List<UserDevice> findByDeviceId(Long deviceId);
    List<UserDevice> findByDeviceIdAndActive(Long deviceId, boolean active);
    List<UserDevice> findByUserSubscriptionId(Long userSubscriptionId);
    void delete(Long id);
}

