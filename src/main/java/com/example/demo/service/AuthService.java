package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Step 1: Add SLF4J
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j // Step 2: Annotate for easy logging
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public String signup(String email, String password) {
        log.info("Signup request received for email: {}", email);
        
        // 1. Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Signup failed: User {} already exists", email);
            return "User already exists";
        }

        // 2. Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // 3. Build the User object
        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        userRepository.save(newUser);
        log.info("User record created for {}. Attempting to send OTP email.", email);
        
        // 4. Send Email with Error Catching
        try {
            emailService.sendOtpEmail(email, otp);
            log.info("OTP email successfully sent to {}", email);
        } catch (Exception e) {
            log.error("ERROR: Failed to send OTP to {}. Message: {}", email, e.getMessage());
            // We return success for the DB save, but warn about the email
            return "Registration successful, but OTP email failed to send. Please try Forgot Password.";
        }

        return "OTP sent to your email. Please verify.";
    }

    public String verifyOtp(String email, String otp) {
        log.info("Verifying OTP for user: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            log.error("Verification failed: User {} not found in database", email);
            return "User not found.";
        }

        User user = userOpt.get();
        if (user.getOtp() != null && user.getOtp().equals(otp) && 
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {

            user.setVerified(true);
            user.setOtp(null); 
            userRepository.save(user);
            log.info("User {} verified successfully.", email);
            return "Account verified successfully.";
        }
        
        log.warn("Invalid or expired OTP attempt for user: {}", email);
        return "Invalid or expired OTP.";
    }

    public String login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Login failed: Email {} not found", email);
            return "Username not found. Please check your email.";
        }

        User user = userOpt.get();

        if (!user.isVerified()) {
            log.warn("Login failed: User {} has not verified their account", email);
            return "Account not verified. Please verify your OTP first.";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: Incorrect password for {}", email);
            return "Password wrong. Please try again.";
        }

        log.info("Login successful for {}. Generating JWT.", email);
        return jwtUtil.generateToken(email);
    }

    public String forgotPassword(String email) {
        log.info("Forgot password request for: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            log.warn("Forgot password failed: {} does not exist", email);
            return "User not found with this email.";
        }

        User user = userOpt.get();
        String otp = String.format("%06d", new Random().nextInt(999999));

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(email, otp);
            log.info("Reset OTP sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send Reset OTP to {}: {}", email, e.getMessage());
            return "Error sending email. Please try again later.";
        }
        
        return "OTP sent successfully to " + email;
    }

    public String verifyResetOtp(String email, String otp) {
        log.info("Verifying reset OTP for: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) return "User not found.";

        User user = userOpt.get();
        if (user.getOtp() != null && user.getOtp().equals(otp) && 
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            log.info("Reset OTP verified for {}", email);
            return "OTP verified. You can now reset your password.";
        }
        
        log.warn("Reset OTP verification failed for {}", email);
        return "Invalid or expired OTP.";
    }

    public String resetPassword(String email, String otp, String newPassword) {
        log.info("Resetting password for: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) return "User not found.";

        User user = userOpt.get();

        if (user.getOtp() != null && user.getOtp().equals(otp) && 
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtp(null); 
            user.setVerified(true);
            userRepository.save(user);
            log.info("Password successfully reset for {}", email);
            return "Password reset successfully.";
        }
        
        log.error("Final password reset check failed for {}. OTP invalid or expired.", email);
        return "Invalid OTP or Session expired.";
    }
}
