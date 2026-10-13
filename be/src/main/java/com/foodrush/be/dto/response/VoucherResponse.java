package com.foodrush.be.dto.response;

import com.foodrush.be.entity.Voucher;

public record VoucherResponse(
        Long id,
        String name,
        String description,
        String discountValue,
        int priceCoins,
        int initialStock,
        int remainingStock,
        long issuedCount,
        boolean active,
        boolean outOfStock,
        String imageUrl,
        String imagePublicId) {

    public static VoucherResponse from(Voucher voucher, long issuedCount) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getName(),
                voucher.getDescription(),
                voucher.getDiscountValue(),
                voucher.getPriceCoins(),
                voucher.getInitialStock(),
                voucher.getRemainingStock(),
                issuedCount,
                voucher.isActive(),
                voucher.getRemainingStock() <= 0,
                voucher.getImageUrl(),
                voucher.getImagePublicId());
    }
}