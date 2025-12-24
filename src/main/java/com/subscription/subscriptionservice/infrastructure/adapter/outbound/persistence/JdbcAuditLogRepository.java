package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.AuditLogRepositoryPort;
import com.subscription.subscriptionservice.domain.model.AuditLog;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcAuditLogRepository extends BaseJdbcRepository implements AuditLogRepositoryPort {

    public JdbcAuditLogRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (auditLog.getId() == null) {
                String sql = "INSERT INTO audit_logs (entity_type, entity_id, action, user_id, username, user_role, " +
                           "description, old_values, new_values, ip_address, request_method, request_path, " +
                           "timestamp, success, error_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, auditLog.getEntityType());
                    stmt.setObject(2, auditLog.getEntityId(), Types.BIGINT);
                    stmt.setString(3, auditLog.getAction());
                    stmt.setObject(4, auditLog.getUserId(), Types.BIGINT);
                    stmt.setString(5, auditLog.getUsername());
                    stmt.setString(6, auditLog.getUserRole());
                    stmt.setString(7, auditLog.getDescription());
                    stmt.setString(8, auditLog.getOldValues());
                    stmt.setString(9, auditLog.getNewValues());
                    stmt.setString(10, auditLog.getIpAddress());
                    stmt.setString(11, auditLog.getRequestMethod());
                    stmt.setString(12, auditLog.getRequestPath());
                    stmt.setTimestamp(13, Timestamp.valueOf(
                        auditLog.getTimestamp() != null ? auditLog.getTimestamp() : LocalDateTime.now()));
                    stmt.setBoolean(14, auditLog.getSuccess() != null ? auditLog.getSuccess() : true);
                    stmt.setString(15, auditLog.getErrorMessage());
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            auditLog.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE audit_logs SET entity_type=?, entity_id=?, action=?, user_id=?, username=?, " +
                           "user_role=?, description=?, old_values=?, new_values=?, ip_address=?, request_method=?, " +
                           "request_path=?, timestamp=?, success=?, error_message=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, auditLog.getEntityType());
                    stmt.setObject(2, auditLog.getEntityId(), Types.BIGINT);
                    stmt.setString(3, auditLog.getAction());
                    stmt.setObject(4, auditLog.getUserId(), Types.BIGINT);
                    stmt.setString(5, auditLog.getUsername());
                    stmt.setString(6, auditLog.getUserRole());
                    stmt.setString(7, auditLog.getDescription());
                    stmt.setString(8, auditLog.getOldValues());
                    stmt.setString(9, auditLog.getNewValues());
                    stmt.setString(10, auditLog.getIpAddress());
                    stmt.setString(11, auditLog.getRequestMethod());
                    stmt.setString(12, auditLog.getRequestPath());
                    stmt.setTimestamp(13, Timestamp.valueOf(
                        auditLog.getTimestamp() != null ? auditLog.getTimestamp() : LocalDateTime.now()));
                    stmt.setBoolean(14, auditLog.getSuccess() != null ? auditLog.getSuccess() : true);
                    stmt.setString(15, auditLog.getErrorMessage());
                    stmt.setLong(16, auditLog.getId());
                    stmt.executeUpdate();
                }
            }
            return auditLog;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving audit log", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        String sql = "SELECT * FROM audit_logs WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit log by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findAll(int page, int size) {
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        String sql = "SELECT * FROM audit_logs WHERE entity_type = ? AND entity_id = ? ORDER BY timestamp DESC";
        return findAuditLogs(sql, entityType, entityId);
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs by user", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByAction(String action) {
        String sql = "SELECT * FROM audit_logs WHERE action = ? ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, action);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs by action", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByEntityType(String entityType) {
        String sql = "SELECT * FROM audit_logs WHERE entity_type = ? ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entityType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs by entity type", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM audit_logs WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs by date range", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findFailed() {
        String sql = "SELECT * FROM audit_logs WHERE success = false ORDER BY timestamp DESC";
        return findAuditLogs(sql);
    }

    @Override
    public List<AuditLog> search(String keyword) {
        String sql = "SELECT * FROM audit_logs WHERE description LIKE ? OR username LIKE ? OR entity_type LIKE ? " +
                   "ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching audit logs", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            // Total logs
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM audit_logs");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalLogs", rs.getLong("total"));
                }
            }
            
            // Successful logs
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM audit_logs WHERE success = true");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("successfulLogs", rs.getLong("total"));
                }
            }
            
            // Failed logs
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM audit_logs WHERE success = false");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("failedLogs", rs.getLong("total"));
                }
            }
            
            // Logs by action
            Map<String, Long> actionCounts = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT action, COUNT(*) as count FROM audit_logs GROUP BY action");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    actionCounts.put(rs.getString("action"), rs.getLong("count"));
                }
            }
            stats.put("logsByAction", actionCounts);
            
        } catch (SQLException e) {
            throw new RuntimeException("Error getting audit log statistics", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return stats;
    }

    private List<AuditLog> findAuditLogs(String sql) {
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapRowToAuditLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    private List<AuditLog> findAuditLogs(String sql, String param1, Long param2) {
        List<AuditLog> logs = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param1);
            stmt.setLong(2, param2);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit logs", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return logs;
    }

    private AuditLog mapRowToAuditLog(ResultSet rs) throws SQLException {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(rs.getLong("id"));
        auditLog.setEntityType(rs.getString("entity_type"));
        Long entityId = rs.getLong("entity_id");
        if (!rs.wasNull()) {
            auditLog.setEntityId(entityId);
        }
        auditLog.setAction(rs.getString("action"));
        Long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            auditLog.setUserId(userId);
        }
        auditLog.setUsername(rs.getString("username"));
        auditLog.setUserRole(rs.getString("user_role"));
        auditLog.setDescription(rs.getString("description"));
        auditLog.setOldValues(rs.getString("old_values"));
        auditLog.setNewValues(rs.getString("new_values"));
        auditLog.setIpAddress(rs.getString("ip_address"));
        auditLog.setRequestMethod(rs.getString("request_method"));
        auditLog.setRequestPath(rs.getString("request_path"));
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            auditLog.setTimestamp(timestamp.toLocalDateTime());
        }
        auditLog.setSuccess(rs.getBoolean("success"));
        auditLog.setErrorMessage(rs.getString("error_message"));
        return auditLog;
    }
}

