package com.foodrush.be.repository;

import com.foodrush.be.entity.CouponRedemption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    boolean existsByCouponId(Long couponId);

    List<CouponRedemption> findAllByOrderByRedeemedAtDesc();

    List<CouponRedemption> findByStaffIdOrderByRedeemedAtDesc(Long staffId);
}
