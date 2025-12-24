package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.SubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.model.Subscription;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSubscriptionRepository extends BaseJdbcRepository implements SubscriptionRepositoryPort {

    public JdbcSubscriptionRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Subscription save(Subscription subscription) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (subscription.getId() == null) {
                String sql = "INSERT INTO subscriptions (name, description, device_id, base_price, subscription_level, " +
                           "billing_cycle, active, deleted, deleted_at, deleted_by, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, subscription.getName());
                    stmt.setString(2, subscription.getDescription());
                    stmt.setLong(3, subscription.getDeviceId());
                    stmt.setBigDecimal(4, subscription.getBasePrice());
                    stmt.setString(5, subscription.getLevel().name());
                    stmt.setString(6, subscription.getBillingCycle().name());
                    stmt.setBoolean(7, subscription.getActive() != null ? subscription.getActive() : true);
                    stmt.setBoolean(8, subscription.getDeleted() != null ? subscription.getDeleted() : false);
                    if (subscription.getDeletedAt() != null) {
                        stmt.setTimestamp(9, Timestamp.valueOf(subscription.getDeletedAt()));
                    } else {
                        stmt.setNull(9, Types.TIMESTAMP);
                    }
                    stmt.setObject(10, subscription.getDeletedBy(), Types.BIGINT);
                    stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            subscription.setId(rs.getLong(1));
                        }
                    }
                    
                    // Save features
                    if (subscription.getFeatureIds() != null && !subscription.getFeatureIds().isEmpty()) {
                        saveFeatures(subscription.getId(), subscription.getFeatureIds(), conn);
                    }
                }
            } else {
                String sql = "UPDATE subscriptions SET name=?, description=?, device_id=?, base_price=?, " +
                           "subscription_level=?, billing_cycle=?, active=?, deleted=?, deleted_at=?, deleted_by=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, subscription.getName());
                    stmt.setString(2, subscription.getDescription());
                    stmt.setLong(3, subscription.getDeviceId());
                    stmt.setBigDecimal(4, subscription.getBasePrice());
                    stmt.setString(5, subscription.getLevel().name());
                    stmt.setString(6, subscription.getBillingCycle().name());
                    stmt.setBoolean(7, subscription.getActive() != null ? subscription.getActive() : true);
                    stmt.setBoolean(8, subscription.getDeleted() != null ? subscription.getDeleted() : false);
                    if (subscription.getDeletedAt() != null) {
                        stmt.setTimestamp(9, Timestamp.valueOf(subscription.getDeletedAt()));
                    } else {
                        stmt.setNull(9, Types.TIMESTAMP);
                    }
                    stmt.setObject(10, subscription.getDeletedBy(), Types.BIGINT);
                    stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(12, subscription.getId());
                    stmt.executeUpdate();
                }
            }
            
            // Load features
            subscription.setFeatureIds(findFeatureIds(subscription.getId(), conn));
            return subscription;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        String sql = "SELECT * FROM subscriptions WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Subscription subscription = mapRowToSubscription(rs);
                    subscription.setFeatureIds(findFeatureIds(id, conn));
                    return Optional.of(subscription);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding subscription by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<Subscription> findAll() {
        String sql = "SELECT * FROM subscriptions WHERE deleted IS NULL OR deleted = false";
        return findSubscriptions(sql);
    }

    @Override
    public List<Subscription> findActive() {
        String sql = "SELECT * FROM subscriptions WHERE active = true AND (deleted IS NULL OR deleted = false)";
        return findSubscriptions(sql);
    }

    @Override
    public List<Subscription> findByDeviceId(Long deviceId) {
        String sql = "SELECT * FROM subscriptions WHERE device_id = ? AND (deleted IS NULL OR deleted = false)";
        List<Subscription> subscriptions = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, deviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subscription subscription = mapRowToSubscription(rs);
                    subscription.setFeatureIds(findFeatureIds(subscription.getId(), conn));
                    subscriptions.add(subscription);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding subscriptions by device", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return subscriptions;
    }

    @Override
    public List<Subscription> findDeleted() {
        String sql = "SELECT * FROM subscriptions WHERE deleted = true";
        return findSubscriptions(sql);
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscriptions WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void softDelete(Long id, Long deletedBy) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE subscriptions SET deleted = true, deleted_at = ?, deleted_by = ?, active = false WHERE id = ?")) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, deletedBy);
            stmt.setLong(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error soft deleting subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void restore(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE subscriptions SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error restoring subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void addFeature(Long subscriptionId, Long featureId) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO subscription_features (subscription_id, feature_id) VALUES (?, ?)")) {
            stmt.setLong(1, subscriptionId);
            stmt.setLong(2, featureId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding feature to subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public void removeFeature(Long subscriptionId, Long featureId) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM subscription_features WHERE subscription_id = ? AND feature_id = ?")) {
            stmt.setLong(1, subscriptionId);
            stmt.setLong(2, featureId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing feature from subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public List<Long> findFeatureIds(Long subscriptionId) {
        return findFeatureIds(subscriptionId, getConnection());
    }

    private List<Long> findFeatureIds(Long subscriptionId, Connection conn) {
        List<Long> featureIds = new ArrayList<>();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT feature_id FROM subscription_features WHERE subscription_id = ?")) {
            stmt.setLong(1, subscriptionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    featureIds.add(rs.getLong("feature_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding feature IDs", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return featureIds;
    }

    private void saveFeatures(Long subscriptionId, List<Long> featureIds, Connection conn) throws SQLException {
        // Delete existing features
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM subscription_features WHERE subscription_id = ?")) {
            stmt.setLong(1, subscriptionId);
            stmt.executeUpdate();
        }
        
        // Insert new features
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO subscription_features (subscription_id, feature_id) VALUES (?, ?)")) {
            for (Long featureId : featureIds) {
                stmt.setLong(1, subscriptionId);
                stmt.setLong(2, featureId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<Subscription> findSubscriptions(String sql) {
        List<Subscription> subscriptions = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Subscription subscription = mapRowToSubscription(rs);
                subscription.setFeatureIds(findFeatureIds(subscription.getId(), conn));
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding subscriptions", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return subscriptions;
    }

    private Subscription mapRowToSubscription(ResultSet rs) throws SQLException {
        Subscription subscription = new Subscription();
        subscription.setId(rs.getLong("id"));
        subscription.setName(rs.getString("name"));
        subscription.setDescription(rs.getString("description"));
        subscription.setDeviceId(rs.getLong("device_id"));
        subscription.setBasePrice(rs.getBigDecimal("base_price"));
        
        String level = rs.getString("subscription_level");
        if (level != null) {
            subscription.setLevel(Subscription.SubscriptionLevel.valueOf(level));
        }
        
        String billingCycle = rs.getString("billing_cycle");
        if (billingCycle != null) {
            subscription.setBillingCycle(Subscription.BillingCycle.valueOf(billingCycle));
        }
        
        subscription.setActive(rs.getBoolean("active"));
        subscription.setDeleted(rs.getBoolean("deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            subscription.setDeletedAt(deletedAt.toLocalDateTime());
        }
        Long deletedBy = rs.getLong("deleted_by");
        if (!rs.wasNull()) {
            subscription.setDeletedBy(deletedBy);
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            subscription.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            subscription.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return subscription;
    }
}

