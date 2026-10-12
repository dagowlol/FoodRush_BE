package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.LoginRequest;
import com.foodrush.be.dto.request.RegisterRequest;
import com.foodrush.be.dto.response.AuthResponse;
import com.foodrush.be.dto.response.UserResponse;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.security.JwtService;
import com.foodrush.be.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_TAKEN);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }

        String displayName = StringUtils.hasText(request.displayName())
                ? request.displayName().trim()
                : request.username();

        User user = User.builder()
                .username(request.username().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(displayName)
                .role(Role.CUSTOMER)
                .totalScore(0L)
                .build();
        userRepository.save(user);

        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    private AuthResponse issueToken(User user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(user));
    }
}
