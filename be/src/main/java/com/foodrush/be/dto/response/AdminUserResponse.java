package com.foodrush.be.dto.response;

import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Role role,
        long totalScore,
        long coinBalance,
        boolean active,
        Instant createdAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getTotalScore(),
                user.getCoinBalance(),
                user.isActive(),
                user.getCreatedAt());
    }
}