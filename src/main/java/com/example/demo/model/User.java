package com.example.demo.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    @Column(name = "otp_expiry", columnDefinition = "DATETIME")
    private LocalDateTime otpExpiry;

    private boolean verified = false;
}
