package com.foodrush.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
        name = "coupon_redemptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupon_redemptions_coupon", columnNames = "coupon_id")
        },
        indexes = {
                @Index(name = "idx_coupon_redemptions_staff", columnList = "staff_id"),
                @Index(name = "idx_coupon_redemptions_time", columnList = "redeemed_at")
        })
public class CouponRedemption extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coupon_redemptions_coupon"))
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coupon_redemptions_customer"))
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coupon_redemptions_staff"))
    private User staff;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;

    @Column(name = "redeemed_at", nullable = false)
    private Instant redeemedAt;

    @Column(length = 255)
    private String note;
}
