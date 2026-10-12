package com.foodrush.be.dto.response;

import com.foodrush.be.entity.OrderResult;
import java.time.Instant;
import java.util.List;

public record ServeResultResponse(
        Long orderId,
        Long sessionId,
        boolean correct,
        int scoreAwarded,
        List<FoodItemResponse> requiredItems,
        List<FoodItemResponse> servedItems,
        int sessionScore,
        long totalScore,
        Instant servedAt) {

    public static ServeResultResponse from(OrderResult result, int sessionScore, long totalScore) {
        List<FoodItemResponse> required = result.getOrder().getRequiredItems().stream()
                .map(FoodItemResponse::from)
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
        List<FoodItemResponse> served = result.getServedItems().stream()
                .map(FoodItemResponse::from)
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
        return new ServeResultResponse(
                result.getOrder().getId(),
                result.getOrder().getSession().getId(),
                result.isCorrect(),
                result.getScoreAwarded(),
                required,
                served,
                sessionScore,
                totalScore,
                result.getServedAt());
    }
}
