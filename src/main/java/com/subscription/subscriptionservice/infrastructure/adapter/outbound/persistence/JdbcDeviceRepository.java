package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.DeviceRepositoryPort;
import com.subscription.subscriptionservice.domain.model.Device;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcDeviceRepository extends BaseJdbcRepository implements DeviceRepositoryPort {

    public JdbcDeviceRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Device save(Device device) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (device.getId() == null) {
                // Insert
                String sql = "INSERT INTO devices (name, description, device_type, active, deleted, deleted_at, deleted_by, api_key, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, device.getName());
                    stmt.setString(2, device.getDescription());
                    stmt.setString(3, device.getDeviceType());
                    stmt.setBoolean(4, device.getActive() != null ? device.getActive() : true);
                    stmt.setBoolean(5, device.getDeleted() != null ? device.getDeleted() : false);
                    if (device.getDeletedAt() != null) {
                        stmt.setTimestamp(6, Timestamp.valueOf(device.getDeletedAt()));
                    } else {
                        stmt.setNull(6, Types.TIMESTAMP);
                    }
                    stmt.setObject(7, device.getDeletedBy(), Types.BIGINT);
                    String apiKey = device.getApiKey();
                    if (apiKey == null) {
                        apiKey = generateApiKey();
                    }
                    stmt.setString(8, apiKey);
                    stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            device.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                // Update
                String sql = "UPDATE devices SET name=?, description=?, device_type=?, active=?, deleted=?, deleted_at=?, deleted_by=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, device.getName());
                    stmt.setString(2, device.getDescription());
                    stmt.setString(3, device.getDeviceType());
                    stmt.setBoolean(4, device.getActive() != null ? device.getActive() : true);
                    stmt.setBoolean(5, device.getDeleted() != null ? device.getDeleted() : false);
                    if (device.getDeletedAt() != null) {
                        stmt.setTimestamp(6, Timestamp.valueOf(device.getDeletedAt()));
                    } else {
                        stmt.setNull(6, Types.TIMESTAMP);
                    }
                    stmt.setObject(7, device.getDeletedBy(), Types.BIGINT);
                    stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(9, device.getId());
                    
                    stmt.executeUpdate();
                }
            }
            return device;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<Device> findById(Long id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding device by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Device> findByApiKey(String apiKey) {
        String sql = "SELECT * FROM devices WHERE api_key = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apiKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDevice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding device by api key", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<Device> findAll() {
        String sql = "SELECT * FROM devices";
        return findDevices(sql);
    }

    @Override
    public List<Device> findActive() {
        String sql = "SELECT * FROM devices WHERE active = true AND (deleted IS NULL OR deleted = false)";
        return findDevices(sql);
    }

    @Override
    public List<Device> findDeleted() {
        String sql = "SELECT * FROM devices WHERE deleted = true";
        return findDevices(sql);
    }

    private List<Device> findDevices(String sql) {
        List<Device> devices = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                devices.add(mapRowToDevice(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding devices", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return devices;
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM devices WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void softDelete(Long id, Long deletedBy) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE devices SET deleted = true, deleted_at = ?, deleted_by = ?, active = false WHERE id = ?")) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, deletedBy);
            stmt.setLong(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error soft deleting device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void restore(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE devices SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error restoring device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Device mapRowToDevice(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getLong("id"));
        device.setName(rs.getString("name"));
        device.setDescription(rs.getString("description"));
        device.setDeviceType(rs.getString("device_type"));
        device.setActive(rs.getBoolean("active"));
        device.setDeleted(rs.getBoolean("deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            device.setDeletedAt(deletedAt.toLocalDateTime());
        }
        Long deletedBy = rs.getLong("deleted_by");
        if (!rs.wasNull()) {
            device.setDeletedBy(deletedBy);
        }
        device.setApiKey(rs.getString("api_key"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            device.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            device.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return device;
    }
}

