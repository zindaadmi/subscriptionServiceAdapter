package com.subscription.subscriptionservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model for Billing - Pure POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    private Long id;
    private Long userSubscriptionId;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal baseAmount;
    private BigDecimal negotiatedAmount;
    private BigDecimal proRataAmount;
    private BigDecimal totalAmount;
    private LocalDate billDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String paymentMethod;
    private BillingStatus status = BillingStatus.PENDING;
    private String pdfPath;
    private Boolean emailSent = false;
    private LocalDateTime emailSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum BillingStatus {
        PENDING,
        PAID,
        OVERDUE,
        CANCELLED
    }

    // Domain methods
    public boolean isPending() {
        return status == BillingStatus.PENDING;
    }

    public boolean isPaid() {
        return status == BillingStatus.PAID;
    }

    public boolean isOverdue() {
        return status == BillingStatus.OVERDUE || 
               (status == BillingStatus.PENDING && dueDate.isBefore(LocalDate.now()));
    }

    public void markAsPaid(String paymentMethod) {
        this.status = BillingStatus.PAID;
        this.paidDate = LocalDate.now();
        this.paymentMethod = paymentMethod;
    }

    public void markAsOverdue() {
        if (isPending() && dueDate.isBefore(LocalDate.now())) {
            this.status = BillingStatus.OVERDUE;
        }
    }
}

