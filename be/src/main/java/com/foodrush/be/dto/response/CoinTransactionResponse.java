package com.foodrush.be.dto.response;

import com.foodrush.be.entity.CoinTransaction;
import java.time.Instant;

public record CoinTransactionResponse(
        Long id,
        String type,
        int amount,
        long balanceAfter,
        String description,
        Long gameSessionId,
        Long issuedVoucherId,
        Instant createdAt) {

    public static CoinTransactionResponse from(CoinTransaction transaction) {
        return new CoinTransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getGameSession() == null ? null : transaction.getGameSession().getId(),
                transaction.getIssuedVoucher() == null ? null : transaction.getIssuedVoucher().getId(),
                transaction.getCreatedAt());
    }
}