package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.config.AppProperties;
import com.foodrush.be.dto.request.ClaimCouponRequest;
import com.foodrush.be.dto.response.CouponResponse;
import com.foodrush.be.entity.Coupon;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.CouponStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.CouponRepository;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.util.CouponCodeGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final int MAX_CODE_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CouponCodeGenerator couponCodeGenerator;
    private final RewardPolicy rewardPolicy;
    private final AppProperties appProperties;
    private final ZoneId appZoneId;

    @Transactional
    public CouponResponse claim(Long userId, ClaimCouponRequest request) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now(appZoneId);
        if (couponRepository.existsByUserIdAndIssuedDate(userId, today)) {
            throw new AppException(ErrorCode.COUPON_ALREADY_CLAIMED_TODAY);
        }

        List<Integer> allowed = rewardPolicy.allowedDiscounts(user.getTotalScore());
        if (allowed.isEmpty()) {
            throw new AppException(ErrorCode.INSUFFICIENT_SCORE);
        }

        int discountPercent = resolveDiscount(request, allowed);

        Coupon coupon = Coupon.builder()
                .code(nextUniqueCode())
                .user(user)
                .discountPercent(discountPercent)
                .status(CouponStatus.ACTIVE)
                .issuedDate(today)
                .expiresAt(Instant.now().plus(appProperties.getCoupon().getExpiryDays(), ChronoUnit.DAYS))
                .build();

        try {
            couponRepository.saveAndFlush(coupon);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.COUPON_ALREADY_CLAIMED_TODAY);
        }
        return CouponResponse.from(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> myCoupons(Long userId) {
        return couponRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CouponResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.from(coupon);
    }

    private int resolveDiscount(ClaimCouponRequest request, List<Integer> allowed) {
        Integer requested = request == null ? null : request.discountPercent();
        if (requested == null) {
            return allowed.stream().mapToInt(Integer::intValue).max().orElseThrow();
        }
        if (!allowed.contains(requested)) {
            throw new AppException(ErrorCode.INVALID_DISCOUNT_OPTION);
        }
        return requested;
    }

    private String nextUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = couponCodeGenerator.generate();
            if (!couponRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new AppException(ErrorCode.INTERNAL_ERROR, "Could not generate a unique coupon code");
    }
}
