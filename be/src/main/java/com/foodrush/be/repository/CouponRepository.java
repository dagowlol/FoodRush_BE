package com.foodrush.be.repository;

import com.foodrush.be.entity.Coupon;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Coupon> findByIdAndUserId(Long id, Long userId);

    List<Coupon> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndIssuedDate(Long userId, LocalDate issuedDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.code = :code")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
}
