package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.response.CoinTransactionResponse;
import com.foodrush.be.dto.response.DailyCoinSummaryResponse;
import com.foodrush.be.dto.response.WalletResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.CoinWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wallet")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final CoinWalletService coinWalletService;

    @Operation(summary = "Get the current user's coin balance")
    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> balance() {
        return ResponseEntity.ok(ApiResponse.ok(coinWalletService.getWallet(SecurityUtils.currentUserId())));
    }

    @Operation(summary = "Get daily coin earning summary for current user")
    @GetMapping("/daily-summary")
    public ResponseEntity<ApiResponse<DailyCoinSummaryResponse>> dailySummary() {
        Long userId = SecurityUtils.currentUserId();
        long dailyEarned = coinWalletService.getDailyCoinsEarned(userId);
        int dailyCap = coinWalletService.getDailyCoinCap();
        long balance = coinWalletService.getWallet(userId).balance();
        // A cap of 0 means unlimited daily earnings.
        long remaining = dailyCap > 0 ? Math.max(0, (long) dailyCap - dailyEarned) : -1L;
        return ResponseEntity.ok(ApiResponse.ok(
                new DailyCoinSummaryResponse(dailyEarned, dailyCap, remaining, balance)));
    }

    @Operation(summary = "List the current user's coin transactions")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<CoinTransactionResponse>>> transactions() {
        return ResponseEntity.ok(ApiResponse.ok(coinWalletService.getTransactions(SecurityUtils.currentUserId())));
    }
}