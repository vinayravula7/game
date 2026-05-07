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

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    @Value("${MAIL_SENDER}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otp) {
        log.info("Preparing to send OTP to: {} from: {}", to, fromEmail);

        Email from = new Email(fromEmail);
        String subject = "OTP - Your Secure Verification Code";
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
                log.error("SendGrid API Error Details: {}", response.getBody());
                throw new RuntimeException("SendGrid Error: " + response.getBody());
            } else {
                log.info("OTP successfully sent to {}", to);
            }
            
        } catch (IOException ex) {
            log.error("Network error while calling SendGrid: {}", ex.getMessage());
            throw new RuntimeException("Email delivery failed due to network error", ex);
        }
    }

    private String createEmailTemplate(String otp) {
     StringBuilder sb = new StringBuilder();
sb.append("<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 450px; margin: 20px auto; padding: 30px; border: 1px solid #e0e0e0; border-radius: 15px; background-color: #ffffff; box-shadow: 0 4px 10px rgba(0,0,0,0.05);\">");
sb.append("<div style=\"text-align: center; margin-bottom: 25px;\">");

// Heading with Green color
sb.append("<h2 style=\"color: #28a745; margin: 0; font-size: 24px;\">GoGrab Verification</h2>");
sb.append("</div>");

sb.append("<p style=\"color: #555; font-size: 16px; line-height: 1.6; text-align: center;\">Hello! Thank you for choosing GoGrab. Use the verification code below to complete your registration.</p>");

// Stylish OTP Box - Green and White theme
sb.append("<div style=\"background-color: #f0fff4; padding: 25px; text-align: center; border-radius: 12px; margin: 30px 0; border: 1px dashed #28a745;\">");
sb.append("<span style=\"font-size: 40px; font-weight: bold; letter-spacing: 10px; color: #28a745;\">").append(otp).append("</span>");
sb.append("<p style=\"color: #666; font-size: 12px; margin-top: 15px;\">Valid for 5 minutes only</p>");
sb.append("</div>");

// Footer / Signature
sb.append("<div style=\"border-top: 1px solid #eeeeee; padding-top: 25px; text-align: center;\">");
sb.append("<p style=\"color: #888; font-size: 14px; margin: 0;\">Best regards,</p>");

// Signature Name in Green
sb.append("<p style=\"color: #28a745; font-size: 18px; font-weight: bold; margin: 5px 0 0 0;\">Vinay Ravula</p>");
sb.append("<p style=\"color: #bbb; font-size: 11px; margin-top: 25px;\">This is an automated system message. Please do not reply to this email.</p>");
sb.append("</div>");
sb.append("</div>");
        
        return sb.toString();
    }
}
