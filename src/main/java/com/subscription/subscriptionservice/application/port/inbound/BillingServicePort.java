package com.subscription.subscriptionservice.application.port.inbound;

import com.subscription.subscriptionservice.domain.model.Billing;

import java.time.LocalDate;
import java.util.List;

public interface BillingServicePort {
    Billing generateBill(Long userSubscriptionId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd);
    Billing findById(Long id);
    List<Billing> findAll();
    List<Billing> findByUserSubscriptionId(Long userSubscriptionId);
    List<Billing> findPending();
    List<Billing> findOverdue();
    void markAsPaid(Long id, String paymentMethod);
    void markAsOverdue(Long id);
    void generateMonthlyBills();
    void markOverdueBills();
}

