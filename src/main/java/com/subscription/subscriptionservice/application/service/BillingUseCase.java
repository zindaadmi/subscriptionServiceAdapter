package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.BillingServicePort;
import com.subscription.subscriptionservice.application.port.outbound.BillingRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import com.subscription.subscriptionservice.application.port.outbound.UserSubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.Billing;
import com.subscription.subscriptionservice.domain.model.UserSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BillingUseCase implements BillingServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(BillingUseCase.class);
    
    private final BillingRepositoryPort billingRepository;
    private final UserSubscriptionRepositoryPort userSubscriptionRepository;
    private final TransactionManager transactionManager;
    
    public BillingUseCase(BillingRepositoryPort billingRepository,
                         UserSubscriptionRepositoryPort userSubscriptionRepository,
                         TransactionManager transactionManager) {
        this.billingRepository = billingRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.transactionManager = transactionManager;
    }
    
    @Override
    public Billing generateBill(Long userSubscriptionId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd) {
        logger.info("Generating bill: userSubscriptionId={}, period={} to {}", userSubscriptionId, billingPeriodStart, billingPeriodEnd);
        
        return transactionManager.executeInTransaction(() -> {
            UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new UserNotFoundException("User subscription not found with id: " + userSubscriptionId));
            
            Billing billing = new Billing();
            billing.setUserSubscriptionId(userSubscriptionId);
            billing.setBillingPeriodStart(billingPeriodStart);
            billing.setBillingPeriodEnd(billingPeriodEnd);
            billing.setBaseAmount(userSubscription.getNegotiatedPrice());
            billing.setNegotiatedAmount(userSubscription.getNegotiatedPrice());
            billing.setProRataAmount(BigDecimal.ZERO); // Calculate pro-rata if needed
            billing.setTotalAmount(userSubscription.getNegotiatedPrice());
            billing.setBillDate(LocalDate.now());
            billing.setDueDate(LocalDate.now().plusDays(30)); // 30 days payment term
            billing.setStatus(Billing.BillingStatus.PENDING);
            
            return billingRepository.save(billing);
        });
    }
    
    @Override
    public Billing findById(Long id) {
        return billingRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Billing not found with id: " + id));
    }
    
    @Override
    public List<Billing> findAll() {
        return billingRepository.findAll();
    }
    
    @Override
    public List<Billing> findByUserSubscriptionId(Long userSubscriptionId) {
        return billingRepository.findByUserSubscriptionId(userSubscriptionId);
    }
    
    @Override
    public List<Billing> findPending() {
        return billingRepository.findPending();
    }
    
    @Override
    public List<Billing> findOverdue() {
        return billingRepository.findOverdue();
    }
    
    @Override
    public void markAsPaid(Long id, String paymentMethod) {
        Billing billing = findById(id);
        billing.markAsPaid(paymentMethod);
        billingRepository.save(billing);
    }
    
    @Override
    public void markAsOverdue(Long id) {
        Billing billing = findById(id);
        billing.markAsOverdue();
        billingRepository.save(billing);
    }
    
    @Override
    public void generateMonthlyBills() {
        logger.info("Generating monthly bills for all active subscriptions");
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findActive();
        
        for (UserSubscription subscription : activeSubscriptions) {
            LocalDate billingStart = subscription.getBillingStartDate();
            LocalDate now = LocalDate.now();
            
            // Generate bill if billing date has passed
            if (billingStart.isBefore(now) || billingStart.equals(now)) {
                LocalDate periodStart = billingStart;
                LocalDate periodEnd = billingStart.plusMonths(1);
                
                // Check if bill already exists for this period
                List<Billing> existingBills = billingRepository.findByUserSubscriptionId(subscription.getId());
                boolean billExists = existingBills.stream()
                    .anyMatch(b -> b.getBillingPeriodStart().equals(periodStart));
                
                if (!billExists) {
                    generateBill(subscription.getId(), periodStart, periodEnd);
                    // Update billing start date for next month
                    subscription.setBillingStartDate(periodEnd);
                    userSubscriptionRepository.save(subscription);
                }
            }
        }
    }
    
    @Override
    public void markOverdueBills() {
        logger.info("Marking overdue bills");
        List<Billing> pendingBills = billingRepository.findPending();
        LocalDate today = LocalDate.now();
        
        for (Billing billing : pendingBills) {
            if (billing.getDueDate().isBefore(today)) {
                markAsOverdue(billing.getId());
            }
        }
    }
}

