package com.foodrush.be.dto.response;

import java.util.List;

public record RewardEligibilityResponse(
        boolean eligible,
        boolean claimedToday,
        long totalScore,
        List<RewardOptionResponse> options,
        String message) {
}
