package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Size;

public record RedeemCouponRequest(

        @Size(max = 255, message = "note is too long")
        String note) {
}
