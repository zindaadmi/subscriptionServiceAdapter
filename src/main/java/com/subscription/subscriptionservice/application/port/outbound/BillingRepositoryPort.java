package com.subscription.subscriptionservice.application.port.outbound;

import com.subscription.subscriptionservice.domain.model.Billing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingRepositoryPort {
    Billing save(Billing billing);
    Optional<Billing> findById(Long id);
    List<Billing> findAll();
    List<Billing> findByUserSubscriptionId(Long userSubscriptionId);
    List<Billing> findPending();
    List<Billing> findOverdue();
    List<Billing> findByStatus(Billing.BillingStatus status);
    List<Billing> findByDueDateBefore(LocalDate date);
    void delete(Long id);
}

