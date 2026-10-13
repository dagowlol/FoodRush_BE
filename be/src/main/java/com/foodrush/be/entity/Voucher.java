package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.IssuedVoucherStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
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
        name = "vouchers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vouchers_seed_key", columnNames = "seed_key")
        },
        indexes = {
                @Index(name = "idx_vouchers_active", columnList = "active")
        })
public class Voucher extends BaseEntity {

    @Column(name = "seed_key", length = 64)
    private String seedKey;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "discount_value", length = 120)
    private String discountValue;

    @Column(name = "price_coins", nullable = false)
    private int priceCoins;

    @Column(name = "initial_stock", nullable = false)
    private int initialStock;

    @Column(name = "remaining_stock", nullable = false)
    private int remainingStock;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version;
}