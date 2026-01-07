package com.subscription.subscriptionservice.infrastructure.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.subscription.subscriptionservice.domain.model.Billing;
import com.subscription.subscriptionservice.domain.model.User;
import com.subscription.subscriptionservice.domain.model.UserSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * PDF Generator for Bills
 */
public class BillPdfGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(BillPdfGenerator.class);
    private static final String BILLS_DIRECTORY = "bills";
    
    static {
        // Create bills directory if it doesn't exist
        try {
            Path billsPath = Paths.get(BILLS_DIRECTORY);
            if (!Files.exists(billsPath)) {
                Files.createDirectories(billsPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create bills directory", e);
        }
    }
    
    /**
     * Generate PDF bill and return the file path
     */
    public String generateBillPdf(Billing billing, User user, UserSubscription userSubscription) {
        try {
            // Create PDF file path
            String fileName = String.format("bill_%d_%s.pdf", 
                billing.getId(), 
                billing.getBillDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            String filePath = Paths.get(BILLS_DIRECTORY, fileName).toString();
            
            // Create PDF document
            File file = new File(filePath);
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add header
            Paragraph header = new Paragraph("INVOICE")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(header);
            
            // Company/Service info
            Paragraph companyInfo = new Paragraph()
                .add("Subscription Service\n")
                .add("Billing Department\n")
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(20);
            document.add(companyInfo);
            
            // Bill details
            Paragraph billDetails = new Paragraph()
                .add("Bill Number: " + billing.getId() + "\n")
                .add("Bill Date: " + billing.getBillDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "\n")
                .add("Due Date: " + billing.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "\n")
                .add("Status: " + billing.getStatus().name())
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
            document.add(billDetails);
            
            // Customer info
            Paragraph customerInfo = new Paragraph()
                .add("Bill To:\n")
                .add(user.getUsername() + "\n")
                .add(user.getEmail() != null ? user.getEmail() + "\n" : "")
                .add(user.getMobileNumber() != null ? user.getMobileNumber() : "")
                .setMarginBottom(20);
            document.add(customerInfo);
            
            // Billing period
            Paragraph period = new Paragraph()
                .add("Billing Period: " + 
                    billing.getBillingPeriodStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + 
                    " to " + 
                    billing.getBillingPeriodEnd().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .setMarginBottom(20);
            document.add(period);
            
            // Items table
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            // Table header
            table.addHeaderCell(new Paragraph("Description").setBold());
            table.addHeaderCell(new Paragraph("Base Amount").setBold().setTextAlignment(TextAlignment.RIGHT));
            table.addHeaderCell(new Paragraph("Negotiated Amount").setBold().setTextAlignment(TextAlignment.RIGHT));
            table.addHeaderCell(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT));
            
            // Table row
            String description = String.format("Subscription - %s (Period: %s to %s)",
                userSubscription.getSubscriptionId(),
                billing.getBillingPeriodStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                billing.getBillingPeriodEnd().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            
            table.addCell(new Paragraph(description));
            table.addCell(new Paragraph(formatCurrency(billing.getBaseAmount())).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Paragraph(formatCurrency(billing.getNegotiatedAmount())).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Paragraph(formatCurrency(billing.getTotalAmount())).setTextAlignment(TextAlignment.RIGHT));
            
            document.add(table);
            
            // Total section
            Paragraph total = new Paragraph()
                .add("Total Amount: " + formatCurrency(billing.getTotalAmount()))
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20)
                .setMarginBottom(20);
            document.add(total);
            
            // Payment terms
            Paragraph paymentTerms = new Paragraph()
                .add("Payment Terms: 30 days\n")
                .add("Please pay by: " + billing.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .setMarginTop(30)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(paymentTerms);
            
            // Footer
            Paragraph footer = new Paragraph()
                .add("Thank you for your business!")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(40)
                .setFontSize(10);
            document.add(footer);
            
            document.close();
            
            logger.info("PDF bill generated: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            logger.error("Failed to generate PDF bill for billing id: {}", billing.getId(), e);
            throw new RuntimeException("Failed to generate PDF bill", e);
        }
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("$%.2f", amount);
    }
}

