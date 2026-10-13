package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.response.IssuedVoucherResponse;
import com.foodrush.be.dto.response.RedemptionStatusResponse;
import com.foodrush.be.dto.response.VoucherResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vouchers")
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "List available vouchers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(voucherService.listActive()));
    }

    @Operation(summary = "Check whether the current user can redeem a voucher today")
    @GetMapping("/redemption-status")
    public ResponseEntity<ApiResponse<RedemptionStatusResponse>> redemptionStatus() {
        return ResponseEntity.ok(ApiResponse.ok(voucherService.redemptionStatus(SecurityUtils.currentUserId())));
    }

    @Operation(summary = "Redeem a voucher")
    @PostMapping("/{id}/redeem")
    public ResponseEntity<ApiResponse<IssuedVoucherResponse>> redeem(@PathVariable Long id) {
        IssuedVoucherResponse response = voucherService.redeem(SecurityUtils.currentUserId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Đã đổi voucher", response));
    }
}