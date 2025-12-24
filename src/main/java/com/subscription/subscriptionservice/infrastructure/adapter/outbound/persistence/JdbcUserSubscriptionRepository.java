package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.UserSubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.model.UserSubscription;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserSubscriptionRepository extends BaseJdbcRepository implements UserSubscriptionRepositoryPort {

    public JdbcUserSubscriptionRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public UserSubscription save(UserSubscription userSubscription) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (userSubscription.getId() == null) {
                String sql = "INSERT INTO user_subscriptions (user_id, subscription_id, negotiated_price, start_date, " +
                           "end_date, billing_start_date, status, duration_months, assigned_by, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, userSubscription.getUserId());
                    stmt.setLong(2, userSubscription.getSubscriptionId());
                    stmt.setBigDecimal(3, userSubscription.getNegotiatedPrice());
                    stmt.setDate(4, Date.valueOf(userSubscription.getStartDate()));
                    if (userSubscription.getEndDate() != null) {
                        stmt.setDate(5, Date.valueOf(userSubscription.getEndDate()));
                    } else {
                        stmt.setNull(5, Types.DATE);
                    }
                    stmt.setDate(6, Date.valueOf(userSubscription.getBillingStartDate()));
                    stmt.setString(7, userSubscription.getStatus().name());
                    stmt.setInt(8, userSubscription.getDurationMonths());
                    if (userSubscription.getAssignedBy() != null) {
                        stmt.setLong(9, userSubscription.getAssignedBy());
                    } else {
                        stmt.setNull(9, Types.BIGINT);
                    }
                    stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            userSubscription.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE user_subscriptions SET user_id=?, subscription_id=?, negotiated_price=?, " +
                           "start_date=?, end_date=?, billing_start_date=?, status=?, duration_months=?, " +
                           "assigned_by=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userSubscription.getUserId());
                    stmt.setLong(2, userSubscription.getSubscriptionId());
                    stmt.setBigDecimal(3, userSubscription.getNegotiatedPrice());
                    stmt.setDate(4, Date.valueOf(userSubscription.getStartDate()));
                    if (userSubscription.getEndDate() != null) {
                        stmt.setDate(5, Date.valueOf(userSubscription.getEndDate()));
                    } else {
                        stmt.setNull(5, Types.DATE);
                    }
                    stmt.setDate(6, Date.valueOf(userSubscription.getBillingStartDate()));
                    stmt.setString(7, userSubscription.getStatus().name());
                    stmt.setInt(8, userSubscription.getDurationMonths());
                    if (userSubscription.getAssignedBy() != null) {
                        stmt.setLong(9, userSubscription.getAssignedBy());
                    } else {
                        stmt.setNull(9, Types.BIGINT);
                    }
                    stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(11, userSubscription.getId());
                    stmt.executeUpdate();
                }
            }
            return userSubscription;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<UserSubscription> findById(Long id) {
        String sql = "SELECT * FROM user_subscriptions WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUserSubscription(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user subscription by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<UserSubscription> findAll() {
        String sql = "SELECT * FROM user_subscriptions";
        return findUserSubscriptions(sql);
    }

    @Override
    public List<UserSubscription> findByUserId(Long userId) {
        String sql = "SELECT * FROM user_subscriptions WHERE user_id = ?";
        return findUserSubscriptions(sql, userId);
    }

    @Override
    public List<UserSubscription> findByUserIdAndStatus(Long userId, UserSubscription.SubscriptionStatus status) {
        String sql = "SELECT * FROM user_subscriptions WHERE user_id = ? AND status = ?";
        List<UserSubscription> subscriptions = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subscriptions.add(mapRowToUserSubscription(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user subscriptions by user and status", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return subscriptions;
    }

    @Override
    public List<UserSubscription> findActive() {
        String sql = "SELECT * FROM user_subscriptions WHERE status = 'ACTIVE'";
        return findUserSubscriptions(sql);
    }

    @Override
    public List<UserSubscription> findBySubscriptionId(Long subscriptionId) {
        String sql = "SELECT * FROM user_subscriptions WHERE subscription_id = ?";
        return findUserSubscriptions(sql, subscriptionId);
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM user_subscriptions WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user subscription", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    private List<UserSubscription> findUserSubscriptions(String sql) {
        List<UserSubscription> subscriptions = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                subscriptions.add(mapRowToUserSubscription(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user subscriptions", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return subscriptions;
    }

    private List<UserSubscription> findUserSubscriptions(String sql, Long param) {
        List<UserSubscription> subscriptions = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subscriptions.add(mapRowToUserSubscription(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user subscriptions", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return subscriptions;
    }

    private UserSubscription mapRowToUserSubscription(ResultSet rs) throws SQLException {
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setId(rs.getLong("id"));
        userSubscription.setUserId(rs.getLong("user_id"));
        userSubscription.setSubscriptionId(rs.getLong("subscription_id"));
        userSubscription.setNegotiatedPrice(rs.getBigDecimal("negotiated_price"));
        userSubscription.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            userSubscription.setEndDate(endDate.toLocalDate());
        }
        userSubscription.setBillingStartDate(rs.getDate("billing_start_date").toLocalDate());
        String status = rs.getString("status");
        if (status != null) {
            userSubscription.setStatus(UserSubscription.SubscriptionStatus.valueOf(status));
        }
        userSubscription.setDurationMonths(rs.getInt("duration_months"));
        Long assignedBy = rs.getLong("assigned_by");
        if (!rs.wasNull()) {
            userSubscription.setAssignedBy(assignedBy);
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            userSubscription.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            userSubscription.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return userSubscription;
    }
}

