package com.foodrush.be.dto.response;

import com.foodrush.be.entity.enums.CouponStatus;
import java.time.Instant;
import java.time.LocalDate;

public record CouponValidationResponse(
        String code,
        boolean valid,
        CouponStatus status,
        int discountPercent,
        String ownerName,
        LocalDate issuedDate,
        Instant expiresAt,
        Instant redeemedAt,
        String message) {
}
