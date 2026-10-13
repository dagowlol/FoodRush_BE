package com.foodrush.be.dto.response;

public record DailyCoinSummaryResponse(
        long dailyCoinsEarned,
        int dailyCoinCap,
        long remainingCoinsToday,
        long coinBalance) {
}
