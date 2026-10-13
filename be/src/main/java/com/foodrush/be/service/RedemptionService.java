package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.RedeemCouponRequest;
import com.foodrush.be.dto.response.CouponValidationResponse;
import com.foodrush.be.dto.response.RedemptionResponse;
import com.foodrush.be.entity.Coupon;
import com.foodrush.be.entity.CouponRedemption;
import com.foodrush.be.entity.IssuedVoucher;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.VoucherRedemption;
import com.foodrush.be.entity.enums.CouponStatus;
import com.foodrush.be.entity.enums.IssuedVoucherStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.CouponRedemptionRepository;
import com.foodrush.be.repository.CouponRepository;
import com.foodrush.be.repository.IssuedVoucherRepository;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.repository.VoucherRedemptionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final IssuedVoucherRepository issuedVoucherRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CouponValidationResponse validate(String code) {
        String normalized = normalize(code);
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Code must not be blank");
        }

        // A code can come from either flow, so probe both tables. Coupon codes and
        // issued voucher codes live in separate namespaces but share one input field
        // at the counter, and both use the same "FS..." format.
        Optional<Coupon> coupon = couponRepository.findByCode(normalized);
        if (coupon.isPresent()) {
            return buildCouponValidation(coupon.get(), Instant.now());
        }

        IssuedVoucher issuedVoucher = issuedVoucherRepository.findByCode(normalized)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        return buildVoucherValidation(issuedVoucher);
    }

    @Transactional
    public RedemptionResponse redeem(Long staffId, String code, RedeemCouponRequest request) {
        String normalized = normalize(code);
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Code must not be blank");
        }

        if (couponRepository.existsByCode(normalized)) {
            return redeemCoupon(staffId, normalized, request);
        }
        return redeemVoucher(staffId, normalized, request);
    }

    private RedemptionResponse redeemCoupon(Long staffId, String code, RedeemCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        Instant now = Instant.now();
        if (coupon.getStatus() == CouponStatus.REDEEMED || coupon.getRedeemedAt() != null) {
            throw new AppException(ErrorCode.COUPON_ALREADY_REDEEMED);
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(now)) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            throw new AppException(ErrorCode.COUPON_EXPIRED);
        }

        User staff = userRepository.getReferenceById(staffId);
        coupon.setStatus(CouponStatus.REDEEMED);
        coupon.setRedeemedAt(now);
        coupon.setRedeemedBy(staff);
        couponRepository.save(coupon);

        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .customer(coupon.getUser())
                .staff(staff)
                .discountPercent(coupon.getDiscountPercent())
                .redeemedAt(now)
                .note(request == null ? null : request.note())
                .build();
        redemptionRepository.saveAndFlush(redemption);

        return RedemptionResponse.from(redemption);
    }

    private RedemptionResponse redeemVoucher(Long staffId, String code, RedeemCouponRequest request) {
        IssuedVoucher issuedVoucher = issuedVoucherRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        Instant now = Instant.now();
        if (issuedVoucher.getStatus() == IssuedVoucherStatus.USED || issuedVoucher.getUsedAt() != null) {
            throw new AppException(ErrorCode.COUPON_ALREADY_REDEEMED, "This voucher has already been used");
        }
        if (issuedVoucher.getStatus() == IssuedVoucherStatus.EXPIRED) {
            throw new AppException(ErrorCode.COUPON_EXPIRED, "This voucher has expired");
        }
        if (issuedVoucher.getRedemptionDate().isBefore(LocalDate.now(ZoneId.systemDefault()))) {
            issuedVoucher.setStatus(IssuedVoucherStatus.EXPIRED);
            issuedVoucherRepository.save(issuedVoucher);
            throw new AppException(ErrorCode.COUPON_EXPIRED, "This voucher has expired");
        }

        User staff = userRepository.getReferenceById(staffId);
        issuedVoucher.setStatus(IssuedVoucherStatus.USED);
        issuedVoucher.setUsedAt(now);
        issuedVoucherRepository.save(issuedVoucher);

        VoucherRedemption redemption = VoucherRedemption.builder()
                .issuedVoucher(issuedVoucher)
                .customer(issuedVoucher.getUser())
                .staff(staff)
                .redeemedAt(now)
                .note(request == null ? null : request.note())
                .build();
        voucherRedemptionRepository.saveAndFlush(redemption);

        return RedemptionResponse.from(redemption);
    }

    @Transactional(readOnly = true)
    public List<RedemptionResponse> history() {
        List<RedemptionResponse> couponRedemptions = redemptionRepository.findAllByOrderByRedeemedAtDesc().stream()
                .map(RedemptionResponse::from)
                .toList();
        List<RedemptionResponse> voucherRedemptions = voucherRedemptionRepository.findAllByOrderByRedeemedAtDesc().stream()
                .map(RedemptionResponse::from)
                .toList();
        return Stream.concat(couponRedemptions.stream(), voucherRedemptions.stream())
                .sorted(Comparator.comparing(RedemptionResponse::redeemedAt).reversed())
                .toList();
    }

    private CouponValidationResponse buildCouponValidation(Coupon coupon, Instant now) {
        boolean expired = coupon.getStatus() == CouponStatus.EXPIRED
                || (coupon.getStatus() == CouponStatus.ACTIVE
                        && coupon.getExpiresAt() != null
                        && coupon.getExpiresAt().isBefore(now));
        CouponStatus effectiveStatus = expired ? CouponStatus.EXPIRED : coupon.getStatus();
        boolean valid = effectiveStatus == CouponStatus.ACTIVE;

        String message = switch (effectiveStatus) {
            case ACTIVE -> "Coupon is valid and ready to redeem";
            case REDEEMED -> "This coupon has already been redeemed";
            case EXPIRED -> "This coupon has expired";
        };

        return CouponValidationResponse.coupon(
                coupon.getCode(),
                valid,
                effectiveStatus,
                coupon.getDiscountPercent(),
                coupon.getUser().getDisplayName(),
                coupon.getIssuedDate(),
                coupon.getExpiresAt(),
                coupon.getRedeemedAt(),
                message);
    }

    private CouponValidationResponse buildVoucherValidation(IssuedVoucher issuedVoucher) {
        var voucher = issuedVoucher.getVoucher();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        boolean expired = issuedVoucher.getStatus() == IssuedVoucherStatus.EXPIRED
                || issuedVoucher.getRedemptionDate().isBefore(today);
        boolean alreadyUsed = issuedVoucher.getStatus() == IssuedVoucherStatus.USED
                || issuedVoucher.getUsedAt() != null;
        boolean valid = !expired && !alreadyUsed;

        CouponStatus effectiveStatus;
        if (alreadyUsed) {
            effectiveStatus = CouponStatus.REDEEMED;
        } else if (expired) {
            effectiveStatus = CouponStatus.EXPIRED;
        } else {
            effectiveStatus = CouponStatus.ACTIVE;
        }

        String message = switch (effectiveStatus) {
            case ACTIVE -> "Voucher is valid and ready to redeem";
            case REDEEMED -> "This voucher has already been used";
            case EXPIRED -> "This voucher has expired";
        };

        return new CouponValidationResponse(
                issuedVoucher.getCode(),
                valid,
                effectiveStatus,
                0,
                issuedVoucher.getUser().getDisplayName(),
                issuedVoucher.getRedemptionDate(),
                null,
                issuedVoucher.getUsedAt(),
                message,
                "VOUCHER",
                voucher.getName(),
                voucher.getDiscountValue());
    }

    private String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}