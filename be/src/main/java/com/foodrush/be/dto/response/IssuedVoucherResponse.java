package com.foodrush.be.dto.response;

import com.foodrush.be.entity.IssuedVoucher;
import java.time.Instant;
import java.time.LocalDate;

public record IssuedVoucherResponse(
        Long id,
        String code,
        Long userId,
        Long voucherId,
        String voucherName,
        String discountValue,
        LocalDate redemptionDate,
        Instant issuedAt,
        String status,
        String usageInstructions,
        Instant usedAt) {

    public static IssuedVoucherResponse from(IssuedVoucher issued) {
        return new IssuedVoucherResponse(
                issued.getId(),
                issued.getCode(),
                issued.getUser().getId(),
                issued.getVoucher().getId(),
                issued.getVoucher().getName(),
                issued.getVoucher().getDiscountValue(),
                issued.getRedemptionDate(),
                issued.getIssuedAt(),
                issued.getStatus().name(),
                "Hãy xuất trình mã này tại cửa hàng Five Star để nhận voucher.",
                issued.getUsedAt());
    }
}