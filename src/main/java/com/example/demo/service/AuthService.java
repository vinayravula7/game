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
		Optional<User> existingUser = userRepository.findByEmail(email);

		String otp = String.format("%06d", new Random().nextInt(999999));
		User user;

		if (existingUser.isPresent()) {
			user = existingUser.get();
			// 1. Check if user already exists and is verified
			if (user.isVerified()) {
				return "User already exists with this email id. Please login.";
			}
			// Update the existing unverified user with a new OTP and password
			user.setOtp(otp);
			user.setPassword(passwordEncoder.encode(password));
			user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		} else {
			// 2. Create a brand new user
		User user = User.builder()
    .email(request.getEmail())
    .password(passwordEncoder.encode(request.getPassword()))
    .otp(otp)
    .otpExpiry(LocalDateTime.now().plusMinutes(5))
    .verified(false) // Use 'verified' NOT 'isVerified'
    .build();
		}

		userRepository.save(user);
		emailService.sendOtpEmail(email, otp);
		return "OTP sent successfully to " + email;
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
