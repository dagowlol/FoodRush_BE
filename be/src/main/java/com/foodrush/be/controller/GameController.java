package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.request.ServeOrderRequest;
import com.foodrush.be.dto.response.FoodItemResponse;
import com.foodrush.be.dto.response.OrderResponse;
import com.foodrush.be.dto.response.ScoreResponse;
import com.foodrush.be.dto.response.ServeResultResponse;
import com.foodrush.be.dto.response.SessionHistoryResponse;
import com.foodrush.be.dto.response.SessionResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Game")
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @Operation(summary = "List all available food items")
    @GetMapping("/foods")
    public ResponseEntity<ApiResponse<List<FoodItemResponse>>> foods() {
        return ResponseEntity.ok(ApiResponse.ok(gameService.listFoodItems()));
    }

    @Operation(summary = "Start a new game session")
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession() {
        SessionResponse response = gameService.startSession(SecurityUtils.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Game session started", response));
    }

    @Operation(summary = "Get the next order (returns the current pending order if one exists)")
    @GetMapping("/sessions/{sessionId}/next-order")
    public ResponseEntity<ApiResponse<OrderResponse>> nextOrder(@PathVariable Long sessionId) {
        OrderResponse response = gameService.getNextOrder(SecurityUtils.currentUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Serve an order and let the backend compute the score")
    @PostMapping("/sessions/{sessionId}/orders/{orderId}/serve")
    public ResponseEntity<ApiResponse<ServeResultResponse>> serve(
            @PathVariable Long sessionId,
            @PathVariable Long orderId,
            @Valid @RequestBody ServeOrderRequest request) {
        ServeResultResponse response = gameService.serveOrder(
                SecurityUtils.currentUserId(), sessionId, orderId, request.servedItemIds());
        return ResponseEntity.ok(ApiResponse.ok("Order served", response));
    }

    @Operation(summary = "Finish a game session")
    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<ApiResponse<SessionResponse>> finish(@PathVariable Long sessionId) {
        SessionResponse response = gameService.finishSession(SecurityUtils.currentUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Game session finished", response));
    }

    @Operation(summary = "Get the current user's game history")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SessionHistoryResponse>>> history() {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getHistory(SecurityUtils.currentUserId())));
    }

    @Operation(summary = "Get the current user's total score")
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<ScoreResponse>> score() {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getScore(SecurityUtils.currentUserId())));
    }
}
