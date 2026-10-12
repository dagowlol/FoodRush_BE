package com.foodrush.be.dto.response;

import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.enums.SessionStatus;
import java.time.Instant;

public record SessionResponse(
        Long sessionId,
        SessionStatus status,
        int scoreEarned,
        int ordersServed,
        Instant startedAt,
        Instant finishedAt) {

    public static SessionResponse from(GameSession session) {
        return new SessionResponse(
                session.getId(),
                session.getStatus(),
                session.getScoreEarned(),
                session.getOrdersServed(),
                session.getStartedAt(),
                session.getFinishedAt());
    }
}
