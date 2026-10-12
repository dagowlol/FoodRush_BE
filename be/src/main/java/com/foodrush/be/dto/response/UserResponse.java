package com.foodrush.be.dto.response;

import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Role role,
        long totalScore) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getTotalScore());
    }
}
