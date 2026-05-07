package com.example.demo.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otp) {
        log.info("Sending OTP via SendGrid API to: {}", to);

        Email from = new Email(fromEmail);
        String subject = "Your Secure Verification Code";
        Email recipient = new Email(to);
        Content content = new Content("text/html", createEmailTemplate(otp));
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            log.info("SendGrid Response Status: {}", response.getStatusCode());
            if (response.getStatusCode() >= 400) {
                log.error("SendGrid Error: {}", response.getBody());
                throw new RuntimeException("API Error: " + response.getBody());
            }
        } catch (IOException ex) {
            log.error("Failed to call SendGrid API: {}", ex.getMessage());
            throw new RuntimeException(ex);
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
