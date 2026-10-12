package com.foodrush.be.dto.response;

import com.foodrush.be.entity.User;

public record LeaderboardEntryResponse(
        int rank,
        Long userId,
        String displayName,
        long totalScore) {

    public static LeaderboardEntryResponse of(int rank, User user) {
        return new LeaderboardEntryResponse(
                rank,
                user.getId(),
                user.getDisplayName(),
                user.getTotalScore());
    }
}
