package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Enables the User.builder() method used in AuthService
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    // --- REQUIRED FIELDS FOR OTP & VERIFICATION ---
    
    // Fixes "cannot find symbol: method getOtp() / setOtp()"
    private String otp; 

    // Fixes "cannot find symbol: method getOtpExpiry() / setOtpExpiry()"
    private LocalDateTime otpExpiry; 

    // Fixes "cannot find symbol: method isVerified() / setVerified()"
    private boolean verified; 

}
