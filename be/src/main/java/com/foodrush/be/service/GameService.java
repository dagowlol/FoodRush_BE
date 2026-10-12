package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.FinishSessionRequest;
import com.foodrush.be.dto.response.LeaderboardEntryResponse;
import com.foodrush.be.dto.response.ScoreResponse;
import com.foodrush.be.dto.response.SessionHistoryResponse;
import com.foodrush.be.dto.response.SessionResponse;
import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.FoodSafetyStatus;
import com.foodrush.be.entity.enums.SessionStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.GameSessionRepository;
import com.foodrush.be.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SessionResponse startSession(Long userId) {
        User user = userRepository.getReferenceById(userId);
        GameSession session = GameSession.builder()
                .user(user)
                .status(SessionStatus.IN_PROGRESS)
                .scoreEarned(0)
                .bugsKilled(0)
                .wavesCompleted(0)
                .foodSafetyStatus(FoodSafetyStatus.SAFE)
                .startedAt(Instant.now())
                .build();
        sessionRepository.save(session);
        return SessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long userId, Long sessionId) {
        GameSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse contaminateFood(Long userId, Long sessionId) {
        GameSession session = sessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.FINISHED) {
            throw new AppException(ErrorCode.SESSION_FINISHED);
        }

        if (session.getFoodSafetyStatus() != FoodSafetyStatus.CONTAMINATED) {
            session.setFoodSafetyStatus(FoodSafetyStatus.CONTAMINATED);
            sessionRepository.save(session);
        }
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse finishSession(Long userId, Long sessionId, FinishSessionRequest request) {
        GameSession session = sessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.FINISHED) {
            return SessionResponse.from(session);
        }

        if (request != null) {
            session.setScoreEarned(request.score());
            session.setBugsKilled(request.bugsKilled());
            session.setWavesCompleted(request.wavesCompleted());
        }
        session.setStatus(SessionStatus.FINISHED);
        session.setFinishedAt(Instant.now());
        sessionRepository.save(session);

        if (session.getScoreEarned() > 0) {
            userRepository.incrementScore(userId, session.getScoreEarned());
        }
        return SessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public List<SessionHistoryResponse> getHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(SessionHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScoreResponse getScore(Long userId) {
        long totalScore = userRepository.findById(userId)
                .map(User::getTotalScore)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return new ScoreResponse(totalScore);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<User> topUsers = userRepository.findTop10ByOrderByTotalScoreDesc();
        List<LeaderboardEntryResponse> entries = new ArrayList<>(topUsers.size());
        for (int i = 0; i < topUsers.size(); i++) {
            entries.add(LeaderboardEntryResponse.of(i + 1, topUsers.get(i)));
        }
        return entries;
    }
}
