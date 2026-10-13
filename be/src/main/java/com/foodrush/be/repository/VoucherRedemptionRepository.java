package com.foodrush.be.repository;

import com.foodrush.be.entity.VoucherRedemption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Long> {

    List<VoucherRedemption> findAllByOrderByRedeemedAtDesc();

    boolean existsByIssuedVoucher_Id(Long issuedVoucherId);
}