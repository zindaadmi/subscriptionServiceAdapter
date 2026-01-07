package com.subscription.subscriptionservice.application.service;

import com.subscription.subscriptionservice.application.port.inbound.BillingServicePort;
import com.subscription.subscriptionservice.application.port.outbound.BillingRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.EmailPort;
import com.subscription.subscriptionservice.application.port.outbound.TransactionManager;
import com.subscription.subscriptionservice.application.port.outbound.UserRepositoryPort;
import com.subscription.subscriptionservice.application.port.outbound.UserSubscriptionRepositoryPort;
import com.subscription.subscriptionservice.domain.exception.UserNotFoundException;
import com.subscription.subscriptionservice.domain.model.Billing;
import com.subscription.subscriptionservice.domain.model.User;
import com.subscription.subscriptionservice.domain.model.UserSubscription;
import com.subscription.subscriptionservice.infrastructure.util.BillPdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillingUseCase implements BillingServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(BillingUseCase.class);
    
    private final BillingRepositoryPort billingRepository;
    private final UserSubscriptionRepositoryPort userSubscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final TransactionManager transactionManager;
    private final BillPdfGenerator pdfGenerator;
    private EmailPort emailPort; // Optional - can be null
    
    public BillingUseCase(BillingRepositoryPort billingRepository,
                         UserSubscriptionRepositoryPort userSubscriptionRepository,
                         UserRepositoryPort userRepository,
                         TransactionManager transactionManager) {
        this.billingRepository = billingRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
        this.transactionManager = transactionManager;
        this.pdfGenerator = new BillPdfGenerator();
        this.emailPort = null; // Will be set if email adapter is registered
    }
    
    // Set email port if available (called by container)
    public void setEmailPort(EmailPort emailPort) {
        this.emailPort = emailPort;
    }
    
    @Override
    public Billing generateBill(Long userSubscriptionId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd) {
        logger.info("Generating bill: userSubscriptionId={}, period={} to {}", userSubscriptionId, billingPeriodStart, billingPeriodEnd);
        
        return transactionManager.executeInTransaction(() -> {
            UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new UserNotFoundException("User subscription not found with id: " + userSubscriptionId));
            
            User user = userRepository.findById(userSubscription.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userSubscription.getUserId()));
            
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
            
            // Save billing first to get the ID
            billing = billingRepository.save(billing);
            
            // Generate PDF
            String pdfPath = null;
            try {
                pdfPath = pdfGenerator.generateBillPdf(billing, user, userSubscription);
                billing.setPdfPath(pdfPath);
                billing = billingRepository.save(billing);
                logger.info("PDF bill generated successfully: {}", pdfPath);
            } catch (Exception e) {
                logger.error("Failed to generate PDF for bill id: {}", billing.getId(), e);
                // Continue even if PDF generation fails
            }
            
            // Send email to client (optional - only if email exists and email service is enabled)
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendBillEmail(billing, user, userSubscription, pdfPath);
            } else {
                logger.info("Skipping email send for bill {} - user {} has no email address", 
                    billing.getId(), user.getUsername());
            }
            
            return billing;
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

    @Override
    public void suspendSubscriptionsWithOverdueBills() {
        logger.info("Suspending subscriptions with overdue bills");
        List<Billing> overdueBills = billingRepository.findOverdue();
        
        for (Billing billing : overdueBills) {
            // Only suspend if bill is overdue for more than 7 days (grace period)
            LocalDate overdueDate = billing.getDueDate();
            LocalDate today = LocalDate.now();
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(overdueDate, today);
            
            if (daysOverdue > 7) { // 7 days grace period
                UserSubscription subscription = userSubscriptionRepository.findById(billing.getUserSubscriptionId())
                    .orElse(null);
                
                if (subscription != null && subscription.isActive()) {
                    subscription.suspend();
                    userSubscriptionRepository.save(subscription);
                    logger.info("Suspended subscription {} due to overdue bill {}", 
                        subscription.getId(), billing.getId());
                }
            }
        }
    }
    
    /**
     * Send bill email to client (optional)
     */
    private void sendBillEmail(Billing billing, User user, UserSubscription userSubscription, String pdfPath) {
        if (emailPort == null || !emailPort.isEnabled()) {
            logger.debug("Email service is not available or disabled, skipping email send");
            return;
        }
        
        try {
            String subject = String.format("Invoice #%d - Subscription Service", billing.getId());
            String body = generateEmailBody(billing, user, userSubscription);
            
            boolean emailSent = emailPort.sendEmail(user.getEmail(), subject, body, pdfPath);
            
            if (emailSent) {
                billing.setEmailSent(true);
                billing.setEmailSentAt(LocalDateTime.now());
                billingRepository.save(billing);
                logger.info("Bill email sent successfully to: {}", user.getEmail());
            } else {
                logger.warn("Failed to send bill email to: {}", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("Error sending bill email to: {}", user.getEmail(), e);
            // Continue even if email sending fails
        }
    }
    
    /**
     * Generate HTML email body for bill
     */
    private String generateEmailBody(Billing billing, User user, UserSubscription userSubscription) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        html.append("<h2 style='color: #2c3e50;'>Invoice Notification</h2>");
        html.append("<p>Dear ").append(user.getUsername()).append(",</p>");
        html.append("<p>Your invoice has been generated for your subscription.</p>");
        
        html.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0;'>Invoice Details</h3>");
        html.append("<p><strong>Invoice Number:</strong> ").append(billing.getId()).append("</p>");
        html.append("<p><strong>Bill Date:</strong> ").append(billing.getBillDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("</p>");
        html.append("<p><strong>Due Date:</strong> ").append(billing.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("</p>");
        html.append("<p><strong>Billing Period:</strong> ")
            .append(billing.getBillingPeriodStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            .append(" to ")
            .append(billing.getBillingPeriodEnd().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            .append("</p>");
        html.append("<p><strong>Total Amount:</strong> <span style='font-size: 18px; color: #27ae60; font-weight: bold;'>$")
            .append(billing.getTotalAmount()).append("</span></p>");
        html.append("</div>");
        
        html.append("<p>The invoice PDF is attached to this email.</p>");
        html.append("<p>Please make payment by the due date to avoid service interruption.</p>");
        
        html.append("<p style='margin-top: 30px;'>Thank you for your business!</p>");
        html.append("<p>Subscription Service Team</p>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}

