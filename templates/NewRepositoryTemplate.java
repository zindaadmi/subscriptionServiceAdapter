package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.NewEntityRepositoryPort;
import com.subscription.subscriptionservice.domain.model.NewEntity;
import com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence.BaseJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Template for creating a new JDBC repository.
 * 
 * Steps to use:
 * 1. Replace "NewEntity" with your domain model
 * 2. Replace "new_entity" with your table name
 * 3. Implement CRUD methods
 * 4. Register in application.yml under "repositories"
 */
public class JdbcNewEntityRepository extends BaseJdbcRepository implements NewEntityRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(JdbcNewEntityRepository.class);

    public JdbcNewEntityRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public NewEntity save(NewEntity entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private NewEntity insert(NewEntity entity) {
        String sql = "INSERT INTO new_entity (name, created_at, updated_at) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, entity.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getCreatedAt()));
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getUpdatedAt()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1));
                }
            }
            
            return entity;
        } catch (SQLException e) {
            logger.error("Error inserting new entity", e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    private NewEntity update(NewEntity entity) {
        String sql = "UPDATE new_entity SET name = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entity.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getUpdatedAt()));
            stmt.setLong(3, entity.getId());
            
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            logger.error("Error updating new entity", e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Override
    public Optional<NewEntity> findById(Long id) {
        String sql = "SELECT * FROM new_entity WHERE id = ? AND deleted = false";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding new entity by id: " + id, e);
            throw new RuntimeException("Failed to find entity", e);
        }
    }

    @Override
    public List<NewEntity> findAll() {
        String sql = "SELECT * FROM new_entity WHERE deleted = false";
        List<NewEntity> entities = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                entities.add(mapRow(rs));
            }
            
            return entities;
        } catch (SQLException e) {
            logger.error("Error finding all new entities", e);
            throw new RuntimeException("Failed to find entities", e);
        }
    }

    private NewEntity mapRow(ResultSet rs) throws SQLException {
        NewEntity entity = new NewEntity();
        entity.setId(rs.getLong("id"));
        entity.setName(rs.getString("name"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        entity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        entity.setDeleted(rs.getBoolean("deleted"));
        return entity;
    }
}

