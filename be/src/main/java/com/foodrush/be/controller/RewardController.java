package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.response.RewardEligibilityResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rewards")
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "Check which reward the current user is eligible to claim today")
    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<RewardEligibilityResponse>> eligibility() {
        RewardEligibilityResponse response = rewardService.getEligibility(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
