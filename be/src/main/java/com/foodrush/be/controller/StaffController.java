package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.request.RedeemCouponRequest;
import com.foodrush.be.dto.response.CouponValidationResponse;
import com.foodrush.be.dto.response.RedemptionResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Staff")
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final RedemptionService redemptionService;

    @Operation(summary = "Validate a coupon without redeeming it")
    @GetMapping("/coupons/{code}/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validate(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(redemptionService.validate(code)));
    }

    @Operation(summary = "Redeem a coupon atomically")
    @PostMapping("/coupons/{code}/redeem")
    public ResponseEntity<ApiResponse<RedemptionResponse>> redeem(
            @PathVariable String code,
            @Valid @RequestBody(required = false) RedeemCouponRequest request) {
        RedemptionResponse response = redemptionService.redeem(
                SecurityUtils.currentUserId(), code, request);
        return ResponseEntity.ok(ApiResponse.ok("Coupon redeemed", response));
    }

    @Operation(summary = "List coupon redemption history")
    @GetMapping("/redemptions")
    public ResponseEntity<ApiResponse<List<RedemptionResponse>>> redemptions() {
        return ResponseEntity.ok(ApiResponse.ok(redemptionService.history()));
    }
}
