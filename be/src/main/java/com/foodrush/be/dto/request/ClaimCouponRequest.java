package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ClaimCouponRequest(

        @Min(value = 1, message = "discountPercent must be at least 1")
        @Max(value = 100, message = "discountPercent must be at most 100")
        Integer discountPercent) {
}
