package com.foodrush.be.auth.otp;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.config.AppProperties;
import com.foodrush.be.entity.OtpCode;
import com.foodrush.be.entity.OtpCode.OtpPurpose;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.mail.EmailService;
import com.foodrush.be.repository.OtpCodeRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final ZoneId appZoneId;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void requestOtp(String email) {
        String code = generateCode();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds((long) appProperties.getOtp().getTtlMinutes() * 60L);
        OtpCode otpCode = OtpCode.builder()
                .email(email.toLowerCase())
                .code(code)
                .purpose(OtpPurpose.REGISTER)
                .expiresAt(expiresAt)
                .verified(false)
                .used(false)
                .build();
        otpCodeRepository.save(otpCode);
        emailService.sendOtp(email.toLowerCase(), code);
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        OtpCode otp = otpCodeRepository.findTopByEmailAndCodeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        email.toLowerCase(), code.trim(), OtpPurpose.REGISTER)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Mã OTP không hợp lệ"));
        Instant now = Instant.now();
        if (otp.isExpired(now)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mã OTP đã hết hạn");
        }
        otp.setVerified(true);
        otpCodeRepository.save(otp);
        return true;
    }

    @Transactional
    public void markUsed(OtpCode otp) {
        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    public OtpCode getVerifiedOtp(String email, String code) {
        return otpCodeRepository.findTopByEmailAndCodeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        email.toLowerCase(), code.trim(), OtpPurpose.REGISTER)
                .filter(o -> o.isVerified() && !o.isExpired(Instant.now()) && !o.isUsed())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Vui lòng xác nhận OTP trước"));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(OTP_CHARS.charAt(random.nextInt(OTP_CHARS.length())));
        }
        return sb.toString();
    }
}

