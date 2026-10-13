package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.response.IssuedVoucherResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "My Vouchers")
@RestController
@RequestMapping("/api/my-vouchers")
@RequiredArgsConstructor
public class MyVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "List the current user's issued vouchers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IssuedVoucherResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(voucherService.myVouchers(SecurityUtils.currentUserId())));
    }
}