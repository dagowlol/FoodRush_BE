package com.foodrush.be.dto.response;

import com.foodrush.be.entity.FoodItem;

public record FoodItemResponse(
        Long id,
        String code,
        String name,
        String emoji,
        int points) {

    public static FoodItemResponse from(FoodItem item) {
        return new FoodItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getEmoji(),
                item.getBasePoints());
    }
}
