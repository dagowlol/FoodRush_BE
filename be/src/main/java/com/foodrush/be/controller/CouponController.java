package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.request.ClaimCouponRequest;
import com.foodrush.be.dto.response.CouponResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Coupons")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Claim today's coupon (points and discount are resolved from the database)")
    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<CouponResponse>> claim(
            @Valid @RequestBody(required = false) ClaimCouponRequest request) {
        CouponResponse response = couponService.claim(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đã nhận mã giảm giá", response));
    }

    @Operation(summary = "List the current user's coupons")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> myCoupons() {
        return ResponseEntity.ok(ApiResponse.ok(couponService.myCoupons(SecurityUtils.currentUserId())));
    }

    @Operation(summary = "Get one of the current user's coupons by id")
    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Long couponId) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getCoupon(SecurityUtils.currentUserId(), couponId)));
    }
}
