package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.RedeemCouponRequest;
import com.foodrush.be.dto.response.CouponValidationResponse;
import com.foodrush.be.dto.response.RedemptionResponse;
import com.foodrush.be.entity.Coupon;
import com.foodrush.be.entity.CouponRedemption;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.CouponStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.CouponRedemptionRepository;
import com.foodrush.be.repository.CouponRepository;
import com.foodrush.be.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CouponValidationResponse validate(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        return buildValidation(coupon, Instant.now());
    }

    @Transactional
    public RedemptionResponse redeem(Long staffId, String code, RedeemCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        Instant now = Instant.now();
        if (coupon.getStatus() == CouponStatus.REDEEMED || coupon.getRedeemedAt() != null) {
            throw new AppException(ErrorCode.COUPON_ALREADY_REDEEMED);
        }
        if (coupon.getExpiresAt().isBefore(now)) {
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

    @Transactional(readOnly = true)
    public List<RedemptionResponse> history() {
        return redemptionRepository.findAllByOrderByRedeemedAtDesc().stream()
                .map(RedemptionResponse::from)
                .toList();
    }

    private CouponValidationResponse buildValidation(Coupon coupon, Instant now) {
        boolean expired = coupon.getStatus() == CouponStatus.EXPIRED
                || (coupon.getStatus() == CouponStatus.ACTIVE && coupon.getExpiresAt().isBefore(now));
        CouponStatus effectiveStatus = expired ? CouponStatus.EXPIRED : coupon.getStatus();
        boolean valid = effectiveStatus == CouponStatus.ACTIVE;

        String message = switch (effectiveStatus) {
            case ACTIVE -> "Coupon is valid and ready to redeem";
            case REDEEMED -> "This coupon has already been redeemed";
            case EXPIRED -> "This coupon has expired";
        };

        return new CouponValidationResponse(
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
}
