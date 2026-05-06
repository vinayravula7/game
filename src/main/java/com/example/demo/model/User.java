package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String otp;
    private LocalDateTime otpExpiry;
    
    // Lombok creates .verified(boolean) for the builder
    // and .isVerified() for the getter automatically.
    private boolean verified; 
}
