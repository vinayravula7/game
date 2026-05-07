package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Step 1: Import Slf4j
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Step 2: Add annotation
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        log.info("Preparing to send OTP email to: {}", to);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Use MimeMessageHelper for HTML support
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Secure Verification Code");
            
            log.debug("Generating HTML content for OTP: {}", otp);
            String htmlContent = createEmailTemplate(otp);
            helper.setText(htmlContent, true); 
            
            log.info("Attempting to connect to SMTP server to send email...");
            mailSender.send(message);
            log.info("Email successfully dispatched to {}", to);
            
        } catch (MessagingException e) {
            log.error("CRITICAL ERROR: MessagingException occurred while sending email to {}. Reason: {}", to, e.getMessage());
            throw new RuntimeException("Error sending stylish email", e);
        } catch (Exception e) {
            log.error("UNEXPECTED ERROR: An exception occurred in EmailService for {}. Class: {}, Message: {}", 
                      to, e.getClass().getName(), e.getMessage());
            throw e;
        }
    }

    private String createEmailTemplate(String otp) {
        log.trace("Building StringBuilder for email template box.");
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family: Arial, sans-serif; max-width: 450px; margin: 20px auto; padding: 25px; border: 1px solid #ddd; border-radius: 12px; background-color: #ffffff;\">");
        sb.append("<h2 style=\"color: #333; text-align: center; margin-bottom: 20px;\">Account Verification</h2>");
        sb.append("<p style=\"color: #555; font-size: 15px; line-height: 1.5; text-align: center;\">Hello, thank you for joining! Please use the verification code below to activate your account. This code is active for 5 minutes.</p>");
        
        // Stylish OTP Box
        sb.append("<div style=\"background-color: #f4f7f6; padding: 20px; text-align: center; border-radius: 10px; margin: 25px 0;\">");
        sb.append("<span style=\"font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #1a73e8;\">").append(otp).append("</span>");
        sb.append("</div>");

        // Footer / Signature
        sb.append("<div style=\"border-top: 1px solid #eee; padding-top: 20px; text-align: center;\">");
        sb.append("<p style=\"color: #888; font-size: 14px; margin: 0;\">Best regards,</p>");
        sb.append("<p style=\"color: #1a73e8; font-size: 16px; font-weight: bold; margin: 5px 0 0 0;\">Vinay Ravula</p>");
        sb.append("<p style=\"color: #999; font-size: 11px; margin-top: 20px;\">This is an automated message. Please do not reply.</p>");
        sb.append("</div>");
        sb.append("</div>");
        
        return sb.toString();
    }
}
