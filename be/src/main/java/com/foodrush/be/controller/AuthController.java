package com.foodrush.be.controller;

import com.foodrush.be.auth.otp.OtpService;
import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.LoginRequest;
import com.foodrush.be.dto.request.RegisterRequest;
import com.foodrush.be.dto.request.otp.RegisterRequestOtpRequest;
import com.foodrush.be.dto.request.otp.RegisterVerifyOtpRequest;
import com.foodrush.be.dto.response.AuthResponse;
import com.foodrush.be.dto.response.UserResponse;
import com.foodrush.be.entity.OtpCode;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final UserRepository userRepository;

    @Operation(summary = "Request OTP for registration")
    @PostMapping("/register/request-otp")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody RegisterRequestOtpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_TAKEN);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }
        otpService.requestOtp(request.email());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email"));
    }

    @Operation(summary = "Verify OTP and register")
    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody RegisterVerifyOtpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_TAKEN);
        }
        otpService.verifyOtp(request.email(), request.otp());
        OtpCode otp = otpService.getVerifiedOtp(request.email(), request.otp());
        RegisterRequest reg = new RegisterRequest(request.username(), request.password(), request.displayName());
        AuthResponse response = authService.registerWithOtp(reg, request.email());
        otpService.markUsed(otp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", response));
    }

    @Operation(summary = "Register a new customer account (legacy)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", response));
    }

    @Operation(summary = "Login and obtain a JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    @Operation(summary = "Get the current authenticated user")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(authService.currentUser(SecurityUtils.currentUserId())));
    }
}




