package com.flipkart.clone.service;

import com.flipkart.clone.entity.OtpCode;
import com.flipkart.clone.entity.User;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.OtpCodeRepository;
import com.flipkart.clone.repository.UserRepository;
import com.flipkart.clone.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    // ── REGISTER ─────────────────────────────────────────────────
    public Map<String, Object> register(String name, String email,
                                        String phone, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Phone already registered");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .isActive(true)
                .build();

        userRepository.save(user);

        String accessToken  = jwtUtil.generateAccessToken(email, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── LOGIN ────────────────────────────────────────────────────
    public Map<String, Object> login(String email, String password) {

        // AuthenticationManager validates email + password via BCrypt
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is banned. Contact support.");
        }

        String accessToken  = jwtUtil.generateAccessToken(email, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── SEND OTP ─────────────────────────────────────────────────
    public void sendOtp(String identifier, String purposeStr) {

        OtpCode.OtpPurpose purpose = OtpCode.OtpPurpose.valueOf(purposeStr.toUpperCase());

        // Generate 6-digit OTP
        String code = String.format("%06d", new Random().nextInt(999999));

        OtpCode otp = OtpCode.builder()
                .identifier(identifier)
                .code(code)
                .purpose(purpose)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        otpCodeRepository.save(otp);

        // Send email
        emailService.sendOtpEmail(identifier, code);
    }

    // ── VERIFY OTP ───────────────────────────────────────────────
    public Map<String, Object> verifyOtp(String identifier,
                                         String code,
                                         String purposeStr) {

        OtpCode.OtpPurpose purpose = OtpCode.OtpPurpose.valueOf(purposeStr.toUpperCase());

        OtpCode otp = otpCodeRepository
                .findTopByIdentifierAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        identifier, purpose, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("OTP expired or not found"));

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid OTP");
        }

        // Mark OTP as used
        otp.setIsUsed(true);
        otpCodeRepository.save(otp);

        // If LOGIN purpose — generate tokens
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken  = jwtUtil.generateAccessToken(identifier, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(identifier);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── REFRESH TOKEN ────────────────────────────────────────────
    public Map<String, Object> refreshToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadRequestException("Refresh token is invalid or expired");
        }

        String email = jwtUtil.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken  = jwtUtil.generateAccessToken(email, user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ── ME — get current logged-in user ──────────────────────────
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── Helper: build standard auth response ─────────────────────
    private Map<String, Object> buildAuthResponse(User user,
                                                  String accessToken,
                                                  String refreshToken) {
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("user", Map.of(
                "id",    user.getId(),
                "name",  user.getName(),
                "email", user.getEmail(),
                "role",  user.getRole().name()
        ));
        return response;
    }
}