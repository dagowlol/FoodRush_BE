package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.CoinTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "coin_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coin_txn_game_session", columnNames = "game_session_id"),
                @UniqueConstraint(name = "uk_coin_txn_issued_voucher", columnNames = "issued_voucher_id")
        },
        indexes = {
                @Index(name = "idx_coin_txn_user", columnList = "user_id")
        })
public class CoinTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coin_txn_user"))
    private User user;

    @Column(nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CoinTransactionType type;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id",
            foreignKey = @ForeignKey(name = "fk_coin_txn_game_session"))
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_voucher_id",
            foreignKey = @ForeignKey(name = "fk_coin_txn_issued_voucher"))
    private IssuedVoucher issuedVoucher;

    @Column(name = "created_at_tx", nullable = false)
    @Builder.Default
    private Instant transactionAt = Instant.now();
}