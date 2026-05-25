package com.flipkart.clone.service;

import com.flipkart.clone.entity.OtpCode;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;

    // ── GENERATE and send OTP ─────────────────────────────────────
    @Transactional
    public void generateAndSend(String identifier,
                                String purposeStr) {

        OtpCode.OtpPurpose purpose =
                OtpCode.OtpPurpose.valueOf(purposeStr.toUpperCase());

        // Invalidate previous OTPs for same identifier + purpose
        // (by relying on is_used = false check in repository)

        String code = String.format("%06d",
                new Random().nextInt(999999));

        OtpCode otp = OtpCode.builder()
                .identifier(identifier)
                .code(code)
                .purpose(purpose)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        otpCodeRepository.save(otp);
        emailService.sendOtpEmail(identifier, code);
    }

    // ── VERIFY OTP ────────────────────────────────────────────────
    @Transactional
    public boolean verify(String identifier,
                          String code,
                          String purposeStr) {

        OtpCode.OtpPurpose purpose =
                OtpCode.OtpPurpose.valueOf(purposeStr.toUpperCase());

        OtpCode otp = otpCodeRepository
                .findTopByIdentifierAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        identifier, purpose, LocalDateTime.now())
                .orElseThrow(() ->
                        new BadRequestException(
                                "OTP expired or not found. Please request a new one."));

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid OTP");
        }

        otp.setIsUsed(true);
        otpCodeRepository.save(otp);
        return true;
    }

    // ── CLEANUP expired OTPs — runs every hour ────────────────────
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteByExpiresAtBefore(
                LocalDateTime.now());
        System.out.println("Expired OTPs cleaned up");
    }
}