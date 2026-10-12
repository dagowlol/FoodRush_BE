package com.foodrush.be.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RewardPolicy {

    public static final int FIVE_PERCENT_THRESHOLD = 10;
    public static final int TEN_PERCENT_THRESHOLD = 20;

    public List<Integer> allowedDiscounts(long totalScore) {
        if (totalScore >= TEN_PERCENT_THRESHOLD) {
            return List.of(10);
        }
        if (totalScore >= FIVE_PERCENT_THRESHOLD) {
            return List.of(5);
        }
        return List.of();
    }
}
