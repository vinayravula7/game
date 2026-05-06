package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final JwtUtil jwtUtil;

public String signup(String email, String password) {
    // 1. Check if user already exists
    if (userRepository.findByEmail(email).isPresent()) {
        return "User already exists";
    }

    // 2. Generate OTP
    String otp = String.format("%06d", new java.util.Random().nextInt(999999));

    // 3. Build the User object (Fixed the 'request' and 'isVerified' errors)
    User newUser = User.builder()
            .email(email) // Using 'email' parameter instead of 'request.getEmail()'
            .password(passwordEncoder.encode(password)) // Using 'password' parameter
            .otp(otp)
            .otpExpiry(java.time.LocalDateTime.now().plusMinutes(5))
            .verified(false) // Using 'verified' instead of 'isVerified'
            .build();

    userRepository.save(newUser);
    
    // 4. Send Email (Optional, based on your logic)
    emailService.sendOtpEmail(email, otp);

    return "OTP sent to your email. Please verify.";
}

	public String verifyOtp(String email, String otp) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			return "User not found.";
		}

		User user = userOpt.get();
		if (user.getOtp() != null && user.getOtp().equals(otp) && 
				user.getOtpExpiry().isAfter(LocalDateTime.now())) {

			user.setVerified(true);
			user.setOtp(null); // Clear OTP after success
			userRepository.save(user);
			return "Account verified successfully.";
		}
		return "Invalid or expired OTP.";
	}

	public String login(String email, String password) {
		Optional<User> userOpt = userRepository.findByEmail(email);

		// 1. Check if username exists
		if (userOpt.isEmpty()) {
			return "Username not found. Please check your email.";
		}

		User user = userOpt.get();

		// 2. Check if account is verified
		if (!user.isVerified()) {
			return "Account not verified. Please verify your OTP first.";
		}

		// 3. Check if password is correct
		if (!passwordEncoder.matches(password, user.getPassword())) {
			return "Password wrong. Please try again.";
		}

		// Success - return JWT token
		return jwtUtil.generateToken(email);
	}

	// 1. Request OTP for password reset
	public String forgotPassword(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			return "User not found with this email.";
		}

		User user = userOpt.get();
		String otp = String.format("%06d", new Random().nextInt(999999));

		user.setOtp(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepository.save(user);

		emailService.sendOtpEmail(email, otp);
		return "OTP sent successfully to " + email;
	}

	// 2. Verify OTP for password reset (returns a status)
	public String verifyResetOtp(String email, String otp) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) return "User not found.";

		User user = userOpt.get();
		if (user.getOtp() != null && user.getOtp().equals(otp) && 
				user.getOtpExpiry().isAfter(LocalDateTime.now())) {
			return "OTP verified. You can now reset your password.";
		}
		return "Invalid or expired OTP.";
	}

	// 3. Set the new password
	public String resetPassword(String email, String otp, String newPassword) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) return "User not found.";

		User user = userOpt.get();

		// Final security check: Ensure OTP is still valid before changing password
		if (user.getOtp() != null && user.getOtp().equals(otp) && 
				user.getOtpExpiry().isAfter(LocalDateTime.now())) {

			user.setPassword(passwordEncoder.encode(newPassword));
			user.setOtp(null); // Clear OTP so it can't be used again
			user.setVerified(true);
			userRepository.save(user);
			return "Password reset successfully.";
		}
		return "Invalid OTP or Session expired.";
	}

}
