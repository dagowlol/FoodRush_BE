package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Size;

public record RedeemCouponRequest(

        @Size(max = 255, message = "Ghi chú quá dài")
        String note) {
}
