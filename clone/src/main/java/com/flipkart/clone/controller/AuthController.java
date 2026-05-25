package com.flipkart.clone.controller;

import com.flipkart.clone.entity.User;
import com.flipkart.clone.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── POST /api/auth/register ───────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest req) {

        Map<String, Object> response = authService.register(
                req.getName(), req.getEmail(),
                req.getPhone(), req.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/login ──────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest req) {

        Map<String, Object> response = authService.login(
                req.getEmail(), req.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/otp/send ───────────────────────────────────
    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody OtpSendRequest req) {

        authService.sendOtp(req.getIdentifier(), req.getPurpose());
        return ResponseEntity.ok(Map.of("message",
                "OTP sent to " + req.getIdentifier()));
    }

    // ── POST /api/auth/otp/verify ─────────────────────────────────
    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest req) {

        Map<String, Object> response = authService.verifyOtp(
                req.getIdentifier(), req.getCode(), req.getPurpose()
        );
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/refresh ────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    // ── GET /api/auth/me ─────────────────────────────────────────
    // Returns current logged-in user — needs valid JWT
    @GetMapping("/me")
    public ResponseEntity<User> me(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    // ═══════════════════════════════════════════════════════════════
    // Request body classes — defined inside controller (no DTOs)
    // ═══════════════════════════════════════════════════════════════

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank @Email(message = "Valid email is required")
        private String email;

        private String phone;

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class OtpSendRequest {
        @NotBlank(message = "Email or phone is required")
        private String identifier;

        @NotBlank(message = "Purpose is required: LOGIN, REGISTER, RESET_PASSWORD")
        private String purpose;
    }

    @Data
    public static class OtpVerifyRequest {
        @NotBlank
        private String identifier;

        @NotBlank @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        private String code;

        @NotBlank
        private String purpose;
    }
}