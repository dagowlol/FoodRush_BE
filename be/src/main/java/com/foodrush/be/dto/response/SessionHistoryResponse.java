package com.foodrush.be.dto.response;

import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.enums.SessionStatus;
import java.time.Instant;

public record SessionHistoryResponse(
        Long sessionId,
        SessionStatus status,
        int scoreEarned,
        int ordersServed,
        Instant startedAt,
        Instant finishedAt) {

    public static SessionHistoryResponse from(GameSession session) {
        return new SessionHistoryResponse(
                session.getId(),
                session.getStatus(),
                session.getScoreEarned(),
                session.getOrdersServed(),
                session.getStartedAt(),
                session.getFinishedAt());
    }
}
