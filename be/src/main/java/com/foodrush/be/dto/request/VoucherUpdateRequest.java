package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record VoucherUpdateRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String description,
        @Size(max = 120) String discountValue,
        @Min(0) Integer priceCoins,
        @Min(0) Integer remainingStock,
        Boolean active) {

    public boolean hasPriceUpdate() {
        return priceCoins != null;
    }
}