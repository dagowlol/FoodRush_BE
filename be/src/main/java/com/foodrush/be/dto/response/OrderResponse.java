package com.foodrush.be.dto.response;

import com.foodrush.be.entity.GameOrder;
import com.foodrush.be.entity.enums.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long sessionId,
        int sequenceNo,
        OrderStatus status,
        List<FoodItemResponse> items,
        Instant createdAt) {

    public static OrderResponse from(GameOrder order) {
        List<FoodItemResponse> items = order.getRequiredItems().stream()
                .map(FoodItemResponse::from)
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getSession().getId(),
                order.getSequenceNo(),
                order.getStatus(),
                items,
                order.getCreatedAt());
    }
}
