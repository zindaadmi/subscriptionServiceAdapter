package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.UserDeviceRepositoryPort;
import com.subscription.subscriptionservice.domain.model.UserDevice;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDeviceRepository extends BaseJdbcRepository implements UserDeviceRepositoryPort {

    public JdbcUserDeviceRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public UserDevice save(UserDevice userDevice) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (userDevice.getId() == null) {
                String sql = "INSERT INTO user_devices (user_id, device_id, subscription_id, user_subscription_id, " +
                           "device_serial, purchase_date, active, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, userDevice.getUserId());
                    stmt.setLong(2, userDevice.getDeviceId());
                    stmt.setLong(3, userDevice.getSubscriptionId());
                    stmt.setLong(4, userDevice.getUserSubscriptionId());
                    stmt.setString(5, userDevice.getDeviceSerial());
                    stmt.setDate(6, Date.valueOf(userDevice.getPurchaseDate()));
                    stmt.setBoolean(7, userDevice.getActive() != null ? userDevice.getActive() : true);
                    stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            userDevice.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE user_devices SET user_id=?, device_id=?, subscription_id=?, user_subscription_id=?, " +
                           "device_serial=?, purchase_date=?, active=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userDevice.getUserId());
                    stmt.setLong(2, userDevice.getDeviceId());
                    stmt.setLong(3, userDevice.getSubscriptionId());
                    stmt.setLong(4, userDevice.getUserSubscriptionId());
                    stmt.setString(5, userDevice.getDeviceSerial());
                    stmt.setDate(6, Date.valueOf(userDevice.getPurchaseDate()));
                    stmt.setBoolean(7, userDevice.getActive() != null ? userDevice.getActive() : true);
                    stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(9, userDevice.getId());
                    stmt.executeUpdate();
                }
            }
            return userDevice;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<UserDevice> findById(Long id) {
        String sql = "SELECT * FROM user_devices WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUserDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user device by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserDevice> findByDeviceSerial(String deviceSerial) {
        String sql = "SELECT * FROM user_devices WHERE device_serial = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deviceSerial);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUserDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user device by serial", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<UserDevice> findAll() {
        String sql = "SELECT * FROM user_devices";
        return findUserDevices(sql);
    }

    @Override
    public List<UserDevice> findByUserId(Long userId) {
        String sql = "SELECT * FROM user_devices WHERE user_id = ?";
        return findUserDevices(sql, userId);
    }

    @Override
    public List<UserDevice> findByDeviceId(Long deviceId) {
        String sql = "SELECT * FROM user_devices WHERE device_id = ?";
        return findUserDevices(sql, deviceId);
    }

    @Override
    public List<UserDevice> findByDeviceIdAndActive(Long deviceId, boolean active) {
        String sql = "SELECT * FROM user_devices WHERE device_id = ? AND active = ?";
        List<UserDevice> devices = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, deviceId);
            stmt.setBoolean(2, active);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    devices.add(mapRowToUserDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user devices by device and active", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return devices;
    }

    @Override
    public List<UserDevice> findByUserSubscriptionId(Long userSubscriptionId) {
        String sql = "SELECT * FROM user_devices WHERE user_subscription_id = ?";
        return findUserDevices(sql, userSubscriptionId);
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM user_devices WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    private List<UserDevice> findUserDevices(String sql) {
        List<UserDevice> devices = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                devices.add(mapRowToUserDevice(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user devices", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return devices;
    }

    private List<UserDevice> findUserDevices(String sql, Long param) {
        List<UserDevice> devices = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    devices.add(mapRowToUserDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user devices", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return devices;
    }

    private UserDevice mapRowToUserDevice(ResultSet rs) throws SQLException {
        UserDevice userDevice = new UserDevice();
        userDevice.setId(rs.getLong("id"));
        userDevice.setUserId(rs.getLong("user_id"));
        userDevice.setDeviceId(rs.getLong("device_id"));
        userDevice.setSubscriptionId(rs.getLong("subscription_id"));
        userDevice.setUserSubscriptionId(rs.getLong("user_subscription_id"));
        userDevice.setDeviceSerial(rs.getString("device_serial"));
        userDevice.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
        userDevice.setActive(rs.getBoolean("active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            userDevice.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            userDevice.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return userDevice;
    }
}

