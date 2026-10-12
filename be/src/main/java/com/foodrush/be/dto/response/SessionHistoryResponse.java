package com.foodrush.be.dto.response;

import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.enums.FoodSafetyStatus;
import com.foodrush.be.entity.enums.SessionStatus;
import java.time.Instant;

public record SessionHistoryResponse(
        Long sessionId,
        SessionStatus status,
        int scoreEarned,
        int bugsKilled,
        int wavesCompleted,
        FoodSafetyStatus foodSafetyStatus,
        Instant startedAt,
        Instant finishedAt) {

    public static SessionHistoryResponse from(GameSession session) {
        return new SessionHistoryResponse(
                session.getId(),
                session.getStatus(),
                session.getScoreEarned(),
                session.getBugsKilled(),
                session.getWavesCompleted(),
                session.getFoodSafetyStatus(),
                session.getStartedAt(),
                session.getFinishedAt());
    }
}
