package com.foodrush.be.repository;

import com.foodrush.be.entity.OtpCode;
import com.foodrush.be.entity.OtpCode.OtpPurpose;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    Optional<OtpCode> findTopByEmailAndCodeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String email, String code, OtpPurpose purpose);

    void deleteByExpiresAtBefore(Instant now);
}
