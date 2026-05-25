// ── OtpCode.java ─────────────────────────────────────────────────
package com.flipkart.clone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier;       // email or phone

    @Column(nullable = false)
    private String code;             // 6-digit OTP

    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum OtpPurpose { LOGIN, REGISTER, RESET_PASSWORD }
}