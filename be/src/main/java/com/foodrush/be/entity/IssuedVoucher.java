package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.IssuedVoucherStatus;
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
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
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
        name = "issued_vouchers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_issued_vouchers_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_issued_vouchers_user_day", columnNames = {"user_id", "redemption_date"})
        },
        indexes = {
                @Index(name = "idx_issued_vouchers_user", columnList = "user_id"),
                @Index(name = "idx_issued_vouchers_voucher", columnList = "voucher_id")
        })
public class IssuedVoucher extends BaseEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_issued_vouchers_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_issued_vouchers_voucher"))
    private Voucher voucher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssuedVoucherStatus status = IssuedVoucherStatus.ISSUED;

    @Column(name = "redemption_date", nullable = false)
    private LocalDate redemptionDate;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private Instant issuedAt = Instant.now();

    @Column(name = "used_at")
    private Instant usedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}