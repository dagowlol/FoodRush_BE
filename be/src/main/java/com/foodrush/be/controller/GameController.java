package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.request.FinishSessionRequest;
import com.foodrush.be.dto.response.LeaderboardEntryResponse;
import com.foodrush.be.dto.response.ScoreResponse;
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

    @Operation(summary = "Start a new Food Defender game session")
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession() {
        SessionResponse response = gameService.startSession(SecurityUtils.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Game session started", response));
    }

    @Operation(summary = "Get the current state of a game session")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(@PathVariable Long sessionId) {
        SessionResponse response = gameService.getSession(SecurityUtils.currentUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Mark the food as CONTAMINATED (one-way, cannot return to SAFE)")
    @PostMapping("/sessions/{sessionId}/food-safety/contaminate")
    public ResponseEntity<ApiResponse<SessionResponse>> contaminate(@PathVariable Long sessionId) {
        SessionResponse response = gameService.contaminateFood(SecurityUtils.currentUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Food marked as contaminated", response));
    }

    @Operation(summary = "Finish a game session with the final run results")
    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<ApiResponse<SessionResponse>> finish(
            @PathVariable Long sessionId,
            @Valid @RequestBody(required = false) FinishSessionRequest request) {
        SessionResponse response = gameService.finishSession(SecurityUtils.currentUserId(), sessionId, request);
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

    @Operation(summary = "Get the top players leaderboard")
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> leaderboard() {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getLeaderboard()));
    }
}
