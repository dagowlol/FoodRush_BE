package com.foodrush.be.dto.response;

import com.foodrush.be.entity.CouponRedemption;
import java.time.Instant;

public record RedemptionResponse(
        Long id,
        String couponCode,
        int discountPercent,
        String customerName,
        String staffName,
        Instant redeemedAt,
        String note) {

    public static RedemptionResponse from(CouponRedemption redemption) {
        return new RedemptionResponse(
                redemption.getId(),
                redemption.getCoupon().getCode(),
                redemption.getDiscountPercent(),
                redemption.getCustomer().getDisplayName(),
                redemption.getStaff().getDisplayName(),
                redemption.getRedeemedAt(),
                redemption.getNote());
    }
}
