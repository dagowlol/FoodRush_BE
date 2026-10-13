package com.foodrush.be.dto.response;

import com.foodrush.be.entity.CouponRedemption;
import com.foodrush.be.entity.VoucherRedemption;
import java.time.Instant;

/**
 * A completed staff redemption. {@code codeType} distinguishes codes earned from
 * game rewards ({@code COUPON}) from codes bought with coins ({@code VOUCHER}).
 */
public record RedemptionResponse(
        Long id,
        String couponCode,
        int discountPercent,
        String customerName,
        String staffName,
        Instant redeemedAt,
        String note,
        String codeType,
        String voucherName,
        String discountValue) {

    public static RedemptionResponse from(CouponRedemption redemption) {
        return new RedemptionResponse(
                redemption.getId(),
                redemption.getCoupon().getCode(),
                redemption.getDiscountPercent(),
                redemption.getCustomer().getDisplayName(),
                redemption.getStaff().getDisplayName(),
                redemption.getRedeemedAt(),
                redemption.getNote(),
                "COUPON",
                null,
                null);
    }

    public static RedemptionResponse from(VoucherRedemption redemption) {
        var voucher = redemption.getIssuedVoucher().getVoucher();
        return new RedemptionResponse(
                redemption.getId(),
                redemption.getIssuedVoucher().getCode(),
                0,
                redemption.getCustomer().getDisplayName(),
                redemption.getStaff().getDisplayName(),
                redemption.getRedeemedAt(),
                redemption.getNote(),
                "VOUCHER",
                voucher.getName(),
                voucher.getDiscountValue());
    }
}