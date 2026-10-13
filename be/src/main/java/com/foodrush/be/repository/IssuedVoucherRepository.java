package com.foodrush.be.repository;

import com.foodrush.be.entity.IssuedVoucher;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssuedVoucherRepository extends JpaRepository<IssuedVoucher, Long> {

    List<IssuedVoucher> findByUserIdOrderByIssuedAtDesc(Long userId);

    List<IssuedVoucher> findAllByOrderByIssuedAtDesc();

    Optional<IssuedVoucher> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndRedemptionDate(Long userId, LocalDate redemptionDate);

    List<IssuedVoucher> findByVoucherIdOrderByIssuedAtDesc(Long voucherId);

    @Query("select count(i) from IssuedVoucher i where i.voucher.id = :voucherId")
    long countByVoucherId(@Param("voucherId") Long voucherId);
}