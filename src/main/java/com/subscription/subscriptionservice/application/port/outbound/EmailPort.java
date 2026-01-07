package com.subscription.subscriptionservice.application.port.outbound;

/**
 * Port for email operations
 * This is an outbound port (driven by infrastructure)
 */
public interface EmailPort {
    /**
     * Send email with optional attachment
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (HTML or plain text)
     * @param attachmentPath Optional path to attachment file (PDF, etc.)
     * @return true if email sent successfully, false otherwise
     */
    boolean sendEmail(String to, String subject, String body, String attachmentPath);
    
    /**
     * Check if email service is enabled
     * @return true if email service is enabled and configured
     */
    boolean isEnabled();
}

