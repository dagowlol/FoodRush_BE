package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoucherCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @Size(max = 120) String discountValue,
        @Min(0) int priceCoins,
        @Min(0) int initialStock,
        Boolean active) {
}