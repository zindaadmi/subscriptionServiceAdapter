package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.FeatureRepositoryPort;
import com.subscription.subscriptionservice.domain.model.Feature;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFeatureRepository extends BaseJdbcRepository implements FeatureRepositoryPort {

    public JdbcFeatureRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Feature save(Feature feature) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (feature.getId() == null) {
                String sql = "INSERT INTO features (name, description, feature_code, active, deleted, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, feature.getName());
                    stmt.setString(2, feature.getDescription());
                    stmt.setString(3, feature.getFeatureCode());
                    stmt.setBoolean(4, feature.getActive() != null ? feature.getActive() : true);
                    stmt.setBoolean(5, feature.getDeleted() != null ? feature.getDeleted() : false);
                    stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            feature.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE features SET name=?, description=?, feature_code=?, active=?, deleted=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, feature.getName());
                    stmt.setString(2, feature.getDescription());
                    stmt.setString(3, feature.getFeatureCode());
                    stmt.setBoolean(4, feature.getActive() != null ? feature.getActive() : true);
                    stmt.setBoolean(5, feature.getDeleted() != null ? feature.getDeleted() : false);
                    stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(7, feature.getId());
                    stmt.executeUpdate();
                }
            }
            return feature;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving feature", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<Feature> findById(Long id) {
        String sql = "SELECT * FROM features WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFeature(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding feature by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Feature> findByName(String name) {
        String sql = "SELECT * FROM features WHERE name = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFeature(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding feature by name", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Feature> findByFeatureCode(String featureCode) {
        String sql = "SELECT * FROM features WHERE feature_code = ? AND (deleted IS NULL OR deleted = false)";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, featureCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFeature(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding feature by code", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<Feature> findAll() {
        String sql = "SELECT * FROM features WHERE deleted IS NULL OR deleted = false";
        return findFeatures(sql);
    }

    @Override
    public List<Feature> findActive() {
        String sql = "SELECT * FROM features WHERE active = true AND (deleted IS NULL OR deleted = false)";
        return findFeatures(sql);
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM features WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting feature", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    private List<Feature> findFeatures(String sql) {
        List<Feature> features = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                features.add(mapRowToFeature(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding features", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return features;
    }

    private Feature mapRowToFeature(ResultSet rs) throws SQLException {
        Feature feature = new Feature();
        feature.setId(rs.getLong("id"));
        feature.setName(rs.getString("name"));
        feature.setDescription(rs.getString("description"));
        feature.setFeatureCode(rs.getString("feature_code"));
        feature.setActive(rs.getBoolean("active"));
        feature.setDeleted(rs.getBoolean("deleted"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            feature.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            feature.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return feature;
    }
}

