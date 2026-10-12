package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.response.FoodItemResponse;
import com.foodrush.be.dto.response.OrderResponse;
import com.foodrush.be.dto.response.ScoreResponse;
import com.foodrush.be.dto.response.ServeResultResponse;
import com.foodrush.be.dto.response.SessionHistoryResponse;
import com.foodrush.be.dto.response.SessionResponse;
import com.foodrush.be.entity.FoodItem;
import com.foodrush.be.entity.GameOrder;
import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.OrderResult;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.OrderStatus;
import com.foodrush.be.entity.enums.SessionStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.FoodItemRepository;
import com.foodrush.be.repository.GameOrderRepository;
import com.foodrush.be.repository.GameSessionRepository;
import com.foodrush.be.repository.OrderResultRepository;
import com.foodrush.be.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final int MIN_ORDER_SIZE = 1;
    private static final int MAX_ORDER_SIZE = 3;

    private final GameSessionRepository sessionRepository;
    private final GameOrderRepository orderRepository;
    private final OrderResultRepository orderResultRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    private final Random random = new SecureRandom();

    @Transactional
    public SessionResponse startSession(Long userId) {
        User user = userRepository.getReferenceById(userId);
        GameSession session = GameSession.builder()
                .user(user)
                .status(SessionStatus.IN_PROGRESS)
                .scoreEarned(0)
                .ordersServed(0)
                .startedAt(Instant.now())
                .build();
        sessionRepository.save(session);
        return SessionResponse.from(session);
    }

    @Transactional
    public OrderResponse getNextOrder(Long userId, Long sessionId) {
        GameSession session = sessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.FINISHED) {
            throw new AppException(ErrorCode.SESSION_FINISHED);
        }

        GameOrder pending = orderRepository
                .findFirstBySessionIdAndStatusOrderBySequenceNoDesc(sessionId, OrderStatus.PENDING)
                .orElse(null);
        if (pending != null) {
            return OrderResponse.from(pending);
        }

        GameOrder order = GameOrder.builder()
                .session(session)
                .sequenceNo(orderRepository.findMaxSequenceNo(sessionId) + 1)
                .status(OrderStatus.PENDING)
                .requiredItems(pickRandomItems())
                .build();
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public ServeResultResponse serveOrder(Long userId, Long sessionId, Long orderId, List<Long> servedItemIds) {
        GameSession session = sessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.FINISHED) {
            throw new AppException(ErrorCode.SESSION_FINISHED);
        }

        GameOrder order = orderRepository.findByIdAndSessionId(orderId, sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.SERVED || orderResultRepository.existsByOrderId(orderId)) {
            throw new AppException(ErrorCode.ORDER_ALREADY_SERVED);
        }

        Set<Long> servedIds = servedItemIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (servedIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ORDER_ITEMS, "servedItemIds must not be empty");
        }

        List<FoodItem> servedItems = foodItemRepository.findAllById(servedIds);
        if (servedItems.size() != servedIds.size()) {
            throw new AppException(ErrorCode.INVALID_ORDER_ITEMS, "One or more served items do not exist");
        }

        Set<Long> requiredIds = order.getRequiredItems().stream()
                .map(FoodItem::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        boolean correct = servedIds.equals(requiredIds);
        int scoreAwarded = correct
                ? order.getRequiredItems().stream().mapToInt(FoodItem::getBasePoints).sum()
                : 0;

        Instant now = Instant.now();
        OrderResult result = OrderResult.builder()
                .order(order)
                .user(userRepository.getReferenceById(userId))
                .correct(correct)
                .scoreAwarded(scoreAwarded)
                .servedItems(new LinkedHashSet<>(servedItems))
                .servedAt(now)
                .build();
        orderResultRepository.save(result);

        order.setStatus(OrderStatus.SERVED);
        orderRepository.save(order);

        session.setOrdersServed(session.getOrdersServed() + 1);
        session.setScoreEarned(session.getScoreEarned() + scoreAwarded);
        sessionRepository.save(session);

        if (scoreAwarded > 0) {
            userRepository.incrementScore(userId, scoreAwarded);
        }
        long totalScore = userRepository.findById(userId)
                .map(User::getTotalScore)
                .orElse(0L);

        return ServeResultResponse.from(result, session.getScoreEarned(), totalScore);
    }

    @Transactional
    public SessionResponse finishSession(Long userId, Long sessionId) {
        GameSession session = sessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() != SessionStatus.FINISHED) {
            session.setStatus(SessionStatus.FINISHED);
            session.setFinishedAt(Instant.now());
            sessionRepository.save(session);
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
    public List<FoodItemResponse> listFoodItems() {
        return foodItemRepository.findAllByActiveTrueOrderByIdAsc().stream()
                .map(FoodItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScoreResponse getScore(Long userId) {
        long totalScore = userRepository.findById(userId)
                .map(User::getTotalScore)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return new ScoreResponse(totalScore);
    }

    private Set<FoodItem> pickRandomItems() {
        List<FoodItem> available = foodItemRepository.findAllByActiveTrueOrderByIdAsc();
        if (available.isEmpty()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "No food items are configured");
        }
        int maxSize = Math.min(MAX_ORDER_SIZE, available.size());
        int size = MIN_ORDER_SIZE + random.nextInt(maxSize - MIN_ORDER_SIZE + 1);

        List<FoodItem> shuffled = new ArrayList<>(available);
        Collections.shuffle(shuffled, random);
        return new LinkedHashSet<>(shuffled.subList(0, size));
    }
}
