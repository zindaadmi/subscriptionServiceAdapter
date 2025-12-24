package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.domain.model.Role;
import com.subscription.subscriptionservice.domain.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JDBC implementation of UserRepositoryPort
 * Uses transaction-aware connection management
 */

/**
 * JDBC implementation of UserRepositoryPort
 * This is an outbound adapter (infrastructure layer)
 */
public class JdbcUserRepository implements UserRepositoryPort {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        String sql;
        if (user.getId() == null) {
            sql = "INSERT INTO users (username, email, password, mobile_number, phone_number, address, " +
                  "city, state, zip_code, country, deleted, deleted_at, deleted_by, provider, provider_id, " +
                  "enabled, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE users SET username=?, email=?, password=?, mobile_number=?, phone_number=?, " +
                  "address=?, city=?, state=?, zip_code=?, country=?, deleted=?, deleted_at=?, deleted_by=?, " +
                  "provider=?, provider_id=?, enabled=?, updated_at=? WHERE id=?";
        }

        // Use transaction connection if available, otherwise get new connection
        Connection conn = JdbcTransactionManager.getCurrentConnection();
        boolean shouldClose = false;
        
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get database connection", e);
            }
        }
        
        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int paramIndex = 1;
            stmt.setString(paramIndex++, user.getUsername());
            stmt.setString(paramIndex++, user.getEmail());
            stmt.setString(paramIndex++, user.getPassword());
            stmt.setString(paramIndex++, user.getMobileNumber());
            stmt.setString(paramIndex++, user.getPhoneNumber());
            stmt.setString(paramIndex++, user.getAddress());
            stmt.setString(paramIndex++, user.getCity());
            stmt.setString(paramIndex++, user.getState());
            stmt.setString(paramIndex++, user.getZipCode());
            stmt.setString(paramIndex++, user.getCountry());
            stmt.setBoolean(paramIndex++, user.getDeleted() != null ? user.getDeleted() : false);
            if (user.getDeletedAt() != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(user.getDeletedAt()));
            } else {
                stmt.setNull(paramIndex++, Types.TIMESTAMP);
            }
            stmt.setObject(paramIndex++, user.getDeletedBy(), Types.BIGINT);
            stmt.setString(paramIndex++, user.getProvider() != null ? user.getProvider().name() : "LOCAL");
            stmt.setString(paramIndex++, user.getProviderId());
            stmt.setBoolean(paramIndex++, user.getEnabled() != null ? user.getEnabled() : true);

            if (user.getId() == null) {
                LocalDateTime now = LocalDateTime.now();
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(now));
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(now));
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1));
                    }
                }
            } else {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setLong(paramIndex, user.getId());
                stmt.executeUpdate();
            }
            stmt.close();

            // Save user roles using the same connection
            saveUserRoles(conn, user);

            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
        }
    }

    private void saveUserRoles(Connection conn, User user) throws SQLException {
        // Delete existing roles
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM user_roles WHERE user_id = ?")) {
            stmt.setLong(1, user.getId());
            stmt.executeUpdate();
        }

        // Insert new roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)")) {
                for (Role role : user.getRoles()) {
                    stmt.setLong(1, user.getId());
                    stmt.setLong(2, role.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = (conn == null || JdbcTransactionManager.getCurrentConnection() == null);
        
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get database connection", e);
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
        }
        return Optional.empty();
    }
    
    private Connection getConnection() {
        Connection conn = JdbcTransactionManager.getCurrentConnection();
        if (conn != null) {
            return conn;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    private boolean shouldCloseConnection() {
        return JdbcTransactionManager.getCurrentConnection() == null;
    }
    
    private void closeConnectionIfNeeded(Connection conn, boolean shouldClose) {
        if (shouldClose && conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log but don't throw
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByMobileNumber(String mobileNumber) {
        String sql = "SELECT * FROM users WHERE mobile_number = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mobileNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by mobile number", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll(boolean includeDeleted) {
        String sql = includeDeleted ? "SELECT * FROM users" : "SELECT * FROM users WHERE deleted IS NULL OR deleted = false";
        List<User> users = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs, conn));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return users;
    }

    @Override
    public List<User> findDeleted() {
        String sql = "SELECT * FROM users WHERE deleted = true";
        List<User> users = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs, conn));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding deleted users", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return users;
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    private User mapRowToUser(ResultSet rs, Connection conn) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setMobileNumber(rs.getString("mobile_number"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAddress(rs.getString("address"));
        user.setCity(rs.getString("city"));
        user.setState(rs.getString("state"));
        user.setZipCode(rs.getString("zip_code"));
        user.setCountry(rs.getString("country"));
        user.setDeleted(rs.getBoolean("deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            user.setDeletedAt(deletedAt.toLocalDateTime());
        }
        Long deletedBy = rs.getLong("deleted_by");
        if (!rs.wasNull()) {
            user.setDeletedBy(deletedBy);
        }
        String provider = rs.getString("provider");
        if (provider != null) {
            user.setProvider(User.AuthProvider.valueOf(provider));
        }
        user.setProviderId(rs.getString("provider_id"));
        user.setEnabled(rs.getBoolean("enabled"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            // Can store in a separate field if needed
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            // Can store in a separate field if needed
        }

        // Load roles
        user.setRoles(loadUserRoles(user.getId(), conn));

        return user;
    }

    private Set<Role> loadUserRoles(Long userId, Connection conn) {
        Set<Role> roles = new HashSet<>();
        String sql = "SELECT r.* FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?";
        boolean useTransactionConn = (conn != null);
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get database connection", e);
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    String roleName = rs.getString("name");
                    if (roleName != null) {
                        role.setName(Role.RoleName.valueOf(roleName));
                    }
                    roles.add(role);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user roles", e);
        } finally {
            if (!useTransactionConn && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
        }
        return roles;
    }
}

