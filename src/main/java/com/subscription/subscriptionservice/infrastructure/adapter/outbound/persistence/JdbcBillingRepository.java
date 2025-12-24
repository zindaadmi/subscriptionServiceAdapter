package com.subscription.subscriptionservice.infrastructure.adapter.outbound.persistence;

import com.subscription.subscriptionservice.application.port.outbound.BillingRepositoryPort;
import com.subscription.subscriptionservice.domain.model.Billing;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBillingRepository extends BaseJdbcRepository implements BillingRepositoryPort {

    public JdbcBillingRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Billing save(Billing billing) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try {
            if (billing.getId() == null) {
                String sql = "INSERT INTO billings (user_subscription_id, billing_period_start, billing_period_end, " +
                           "base_amount, negotiated_amount, pro_rata_amount, total_amount, bill_date, due_date, " +
                           "paid_date, payment_method, status, pdf_path, email_sent, email_sent_at, created_at, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, billing.getUserSubscriptionId());
                    stmt.setDate(2, Date.valueOf(billing.getBillingPeriodStart()));
                    stmt.setDate(3, Date.valueOf(billing.getBillingPeriodEnd()));
                    stmt.setBigDecimal(4, billing.getBaseAmount());
                    stmt.setBigDecimal(5, billing.getNegotiatedAmount());
                    stmt.setBigDecimal(6, billing.getProRataAmount());
                    stmt.setBigDecimal(7, billing.getTotalAmount());
                    stmt.setDate(8, Date.valueOf(billing.getBillDate()));
                    stmt.setDate(9, Date.valueOf(billing.getDueDate()));
                    if (billing.getPaidDate() != null) {
                        stmt.setDate(10, Date.valueOf(billing.getPaidDate()));
                    } else {
                        stmt.setNull(10, Types.DATE);
                    }
                    stmt.setString(11, billing.getPaymentMethod());
                    stmt.setString(12, billing.getStatus().name());
                    stmt.setString(13, billing.getPdfPath());
                    stmt.setBoolean(14, billing.getEmailSent() != null ? billing.getEmailSent() : false);
                    if (billing.getEmailSentAt() != null) {
                        stmt.setTimestamp(15, Timestamp.valueOf(billing.getEmailSentAt()));
                    } else {
                        stmt.setNull(15, Types.TIMESTAMP);
                    }
                    stmt.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
                    
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            billing.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE billings SET user_subscription_id=?, billing_period_start=?, billing_period_end=?, " +
                           "base_amount=?, negotiated_amount=?, pro_rata_amount=?, total_amount=?, bill_date=?, " +
                           "due_date=?, paid_date=?, payment_method=?, status=?, pdf_path=?, email_sent=?, " +
                           "email_sent_at=?, updated_at=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, billing.getUserSubscriptionId());
                    stmt.setDate(2, Date.valueOf(billing.getBillingPeriodStart()));
                    stmt.setDate(3, Date.valueOf(billing.getBillingPeriodEnd()));
                    stmt.setBigDecimal(4, billing.getBaseAmount());
                    stmt.setBigDecimal(5, billing.getNegotiatedAmount());
                    stmt.setBigDecimal(6, billing.getProRataAmount());
                    stmt.setBigDecimal(7, billing.getTotalAmount());
                    stmt.setDate(8, Date.valueOf(billing.getBillDate()));
                    stmt.setDate(9, Date.valueOf(billing.getDueDate()));
                    if (billing.getPaidDate() != null) {
                        stmt.setDate(10, Date.valueOf(billing.getPaidDate()));
                    } else {
                        stmt.setNull(10, Types.DATE);
                    }
                    stmt.setString(11, billing.getPaymentMethod());
                    stmt.setString(12, billing.getStatus().name());
                    stmt.setString(13, billing.getPdfPath());
                    stmt.setBoolean(14, billing.getEmailSent() != null ? billing.getEmailSent() : false);
                    if (billing.getEmailSentAt() != null) {
                        stmt.setTimestamp(15, Timestamp.valueOf(billing.getEmailSentAt()));
                    } else {
                        stmt.setNull(15, Types.TIMESTAMP);
                    }
                    stmt.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setLong(17, billing.getId());
                    stmt.executeUpdate();
                }
            }
            return billing;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving billing", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    @Override
    public Optional<Billing> findById(Long id) {
        String sql = "SELECT * FROM billings WHERE id = ?";
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding billing by id", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return Optional.empty();
    }

    @Override
    public List<Billing> findAll() {
        String sql = "SELECT * FROM billings";
        return findBillings(sql);
    }

    @Override
    public List<Billing> findByUserSubscriptionId(Long userSubscriptionId) {
        String sql = "SELECT * FROM billings WHERE user_subscription_id = ?";
        return findBillings(sql, userSubscriptionId);
    }

    @Override
    public List<Billing> findPending() {
        String sql = "SELECT * FROM billings WHERE status = 'PENDING'";
        return findBillings(sql);
    }

    @Override
    public List<Billing> findOverdue() {
        String sql = "SELECT * FROM billings WHERE status = 'OVERDUE' OR (status = 'PENDING' AND due_date < ?)";
        List<Billing> billings = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(mapRowToBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding overdue billings", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return billings;
    }

    @Override
    public List<Billing> findByStatus(Billing.BillingStatus status) {
        String sql = "SELECT * FROM billings WHERE status = ?";
        List<Billing> billings = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(mapRowToBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding billings by status", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return billings;
    }

    @Override
    public List<Billing> findByDueDateBefore(LocalDate date) {
        String sql = "SELECT * FROM billings WHERE due_date < ?";
        List<Billing> billings = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(mapRowToBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding billings by due date", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return billings;
    }

    @Override
    public void delete(Long id) {
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM billings WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting billing", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
    }

    private List<Billing> findBillings(String sql) {
        List<Billing> billings = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                billings.add(mapRowToBilling(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding billings", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return billings;
    }

    private List<Billing> findBillings(String sql, Long param) {
        List<Billing> billings = new ArrayList<>();
        Connection conn = getConnection();
        boolean shouldClose = shouldCloseConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(mapRowToBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding billings", e);
        } finally {
            closeConnectionIfNeeded(conn, shouldClose);
        }
        return billings;
    }

    private Billing mapRowToBilling(ResultSet rs) throws SQLException {
        Billing billing = new Billing();
        billing.setId(rs.getLong("id"));
        billing.setUserSubscriptionId(rs.getLong("user_subscription_id"));
        billing.setBillingPeriodStart(rs.getDate("billing_period_start").toLocalDate());
        billing.setBillingPeriodEnd(rs.getDate("billing_period_end").toLocalDate());
        billing.setBaseAmount(rs.getBigDecimal("base_amount"));
        billing.setNegotiatedAmount(rs.getBigDecimal("negotiated_amount"));
        billing.setProRataAmount(rs.getBigDecimal("pro_rata_amount"));
        billing.setTotalAmount(rs.getBigDecimal("total_amount"));
        billing.setBillDate(rs.getDate("bill_date").toLocalDate());
        billing.setDueDate(rs.getDate("due_date").toLocalDate());
        Date paidDate = rs.getDate("paid_date");
        if (paidDate != null) {
            billing.setPaidDate(paidDate.toLocalDate());
        }
        billing.setPaymentMethod(rs.getString("payment_method"));
        String status = rs.getString("status");
        if (status != null) {
            billing.setStatus(Billing.BillingStatus.valueOf(status));
        }
        billing.setPdfPath(rs.getString("pdf_path"));
        billing.setEmailSent(rs.getBoolean("email_sent"));
        Timestamp emailSentAt = rs.getTimestamp("email_sent_at");
        if (emailSentAt != null) {
            billing.setEmailSentAt(emailSentAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            billing.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            billing.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return billing;
    }
}

