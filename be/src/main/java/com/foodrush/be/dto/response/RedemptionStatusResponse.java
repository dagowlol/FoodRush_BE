package com.foodrush.be.dto.response;

import java.time.LocalDate;

public record RedemptionStatusResponse(
        boolean canRedeemToday,
        LocalDate lastRedemptionDate,
        String message) {
}