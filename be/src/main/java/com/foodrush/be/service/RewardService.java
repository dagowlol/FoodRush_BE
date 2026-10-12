package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.response.RewardEligibilityResponse;
import com.foodrush.be.dto.response.RewardOptionResponse;
import com.foodrush.be.entity.User;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.CouponRepository;
import com.foodrush.be.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final RewardPolicy rewardPolicy;
    private final ZoneId appZoneId;

    @Transactional(readOnly = true)
    public RewardEligibilityResponse getEligibility(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        long totalScore = user.getTotalScore();
        LocalDate today = LocalDate.now(appZoneId);
        boolean claimedToday = couponRepository.existsByUserIdAndIssuedDate(userId, today);

        List<RewardOptionResponse> options = rewardPolicy.allowedDiscounts(totalScore).stream()
                .map(percent -> new RewardOptionResponse(percent, percent + "% off your order"))
                .toList();

        boolean eligible = !claimedToday && !options.isEmpty();
        return new RewardEligibilityResponse(eligible, claimedToday, totalScore, options, message(claimedToday, options));
    }

    private String message(boolean claimedToday, List<RewardOptionResponse> options) {
        if (claimedToday) {
            return "You have already claimed a coupon today. Keep playing to earn more points!";
        }
        if (options.isEmpty()) {
            return "Reach at least " + RewardPolicy.FIVE_PERCENT_THRESHOLD + " points to unlock a 5% coupon.";
        }
        return "You can claim your reward now.";
    }
}
