package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ClaimCouponRequest(

        @Min(value = 1, message = "Phần trăm giảm giá phải ít nhất là 1")
        @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100")
        Integer discountPercent) {
}
