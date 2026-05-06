package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<String> signup(@RequestBody Map<String, String> req) {
		String result = authService.signup(req.get("email"), req.get("password"));

		// If the user already exists and is verified, return a Conflict (409) or OK (200)
		if (result.contains("already exists")) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/verify")
	public ResponseEntity<String> verify(@RequestBody Map<String, String> req) {
		String result = authService.verifyOtp(req.get("email"), req.get("otp"));

		if (result.equals("Account verified successfully.")) {
			return ResponseEntity.ok(result);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody Map<String, String> req) {
		String result = authService.login(req.get("email"), req.get("password"));

		// Logic to differentiate between a JWT token and an error message
		if (result.contains("Username not found")) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
		} else if (result.contains("Account not verified")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
		} else if (result.contains("Password wrong")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
		}

		// If none of the above, it's the JWT token
		return ResponseEntity.ok(result);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> req) {
		String result = authService.forgotPassword(req.get("email"));
		if (result.contains("User not found")) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/verify-reset-otp")
	public ResponseEntity<String> verifyResetOtp(@RequestBody Map<String, String> req) {
		String result = authService.verifyResetOtp(req.get("email"), req.get("otp"));
		if (result.contains("verified")) {
			return ResponseEntity.ok(result);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> req) {
		String result = authService.resetPassword(
				req.get("email"), 
				req.get("otp"), 
				req.get("newPassword")
				);

		if (result.contains("successfully")) {
			return ResponseEntity.ok(result);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
	}

}