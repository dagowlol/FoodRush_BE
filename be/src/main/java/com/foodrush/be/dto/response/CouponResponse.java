package com.foodrush.be.dto.response;

import com.foodrush.be.entity.Coupon;
import com.foodrush.be.entity.enums.CouponStatus;
import java.time.Instant;
import java.time.LocalDate;

public record CouponResponse(
        Long id,
        String code,
        int discountPercent,
        CouponStatus status,
        LocalDate issuedDate,
        Instant expiresAt,
        Instant createdAt,
        Instant redeemedAt) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountPercent(),
                effectiveStatus(coupon),
                coupon.getIssuedDate(),
                coupon.getExpiresAt(),
                coupon.getCreatedAt(),
                coupon.getRedeemedAt());
    }

    private static CouponStatus effectiveStatus(Coupon coupon) {
        if (coupon.getStatus() == CouponStatus.ACTIVE
                && coupon.getExpiresAt() != null
                && coupon.getExpiresAt().isBefore(Instant.now())) {
            return CouponStatus.EXPIRED;
        }
        return coupon.getStatus();
    }
}
