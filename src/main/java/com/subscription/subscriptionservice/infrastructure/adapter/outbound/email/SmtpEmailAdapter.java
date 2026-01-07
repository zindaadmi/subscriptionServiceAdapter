package com.subscription.subscriptionservice.infrastructure.adapter.outbound.email;

import com.subscription.subscriptionservice.application.port.outbound.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * SMTP Email Adapter - Implements EmailPort using JavaMail API
 */
public class SmtpEmailAdapter implements EmailPort {
    
    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailAdapter.class);
    
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromEmail;
    
    public SmtpEmailAdapter(String host, int port, String username, String password, String fromEmail) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        
        // Check if email is properly configured
        this.enabled = host != null && !host.isEmpty() && 
                      username != null && !username.isEmpty() && 
                      password != null && !password.isEmpty() &&
                      fromEmail != null && !fromEmail.isEmpty();
        
        if (!enabled) {
            logger.warn("Email service is not properly configured. Email sending will be disabled.");
        } else {
            logger.info("Email service configured: host={}, port={}, from={}", host, port, fromEmail);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public boolean sendEmail(String to, String subject, String body, String attachmentPath) {
        if (!enabled) {
            logger.debug("Email service is disabled, skipping email send to: {}", to);
            return false;
        }
        
        if (to == null || to.isEmpty()) {
            logger.warn("Cannot send email: recipient email is null or empty");
            return false;
        }
        
        try {
            // Setup mail properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
            
            // Create session
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            
            // Create multipart message
            MimeMultipart multipart = new MimeMultipart();
            
            // Add body
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(body, "text/html; charset=utf-8");
            multipart.addBodyPart(bodyPart);
            
            // Add attachment if provided
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                File attachment = new File(attachmentPath);
                if (attachment.exists() && attachment.isFile()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(attachment);
                    multipart.addBodyPart(attachmentPart);
                    logger.debug("Added attachment to email: {}", attachmentPath);
                } else {
                    logger.warn("Attachment file not found: {}", attachmentPath);
                }
            }
            
            message.setContent(multipart);
            
            // Send email
            Transport.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            return false;
        }
    }
}

