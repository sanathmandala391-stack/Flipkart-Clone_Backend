// OtpCodeRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    // Latest unused, unexpired OTP for an identifier + purpose
    Optional<OtpCode> findTopByIdentifierAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String identifier,
            OtpCode.OtpPurpose purpose,
            LocalDateTime now
    );

    // Clean up expired OTPs — called by scheduler
    void deleteByExpiresAtBefore(LocalDateTime now);
}