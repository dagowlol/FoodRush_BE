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

/**
 * Records a staff member redeeming an {@link IssuedVoucher} that a customer
 * bought with coins in the shop. Kept separate from {@link CouponRedemption}
 * because the two redemption flows reference different entities and store
 * different discount representations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "voucher_redemptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_voucher_redemptions_issued", columnNames = "issued_voucher_id")
        },
        indexes = {
                @Index(name = "idx_voucher_redemptions_staff", columnList = "staff_id"),
                @Index(name = "idx_voucher_redemptions_time", columnList = "redeemed_at")
        })
public class VoucherRedemption extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_voucher_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_redemptions_issued_voucher"))
    private IssuedVoucher issuedVoucher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_redemptions_customer"))
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_redemptions_staff"))
    private User staff;

    @Column(name = "redeemed_at", nullable = false)
    private Instant redeemedAt;

    @Column(length = 255)
    private String note;
}