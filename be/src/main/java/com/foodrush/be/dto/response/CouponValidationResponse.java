package com.foodrush.be.dto.response;

import com.foodrush.be.entity.enums.CouponStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Result of checking a code at the staff portal. Codes come from two different
 * sources, so {@code codeType} tells the portal which one was scanned:
 * {@code COUPON} for codes earned from game rewards, {@code VOUCHER} for codes
 * bought with coins in the shop.
 */
public record CouponValidationResponse(
        String code,
        boolean valid,
        CouponStatus status,
        int discountPercent,
        String ownerName,
        LocalDate issuedDate,
        Instant expiresAt,
        Instant redeemedAt,
        String message,
        String codeType,
        String voucherName,
        String discountValue) {

    public static CouponValidationResponse coupon(
            String code,
            boolean valid,
            CouponStatus status,
            int discountPercent,
            String ownerName,
            LocalDate issuedDate,
            Instant expiresAt,
            Instant redeemedAt,
            String message) {
        return new CouponValidationResponse(
                code, valid, status, discountPercent, ownerName, issuedDate,
                expiresAt, redeemedAt, message, "COUPON", null, null);
    }
}