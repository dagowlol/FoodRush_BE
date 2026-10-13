package com.foodrush.be.auth.oauth;

import com.foodrush.be.config.AppProperties;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.security.JwtService;
import com.foodrush.be.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String sub = (String) attributes.get("sub");

        if (!StringUtils.hasText(email)) {
            email = sub + "@google.local";
        }
        String finalEmail = email.toLowerCase();
        String finalName = StringUtils.hasText(name) ? name : finalEmail.split("@")[0];

        // Find existing user or create a new one (first-time Google login)
        boolean emailVerified = isEmailVerified(attributes);
        boolean isAdminEmail = isAdminEmail(finalEmail);

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    String baseUsername = finalEmail.split("@")[0];
                    String username = baseUsername;
                    int counter = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + counter++;
                    }
                    return userRepository.save(User.builder()
                            .username(username)
                            .email(finalEmail)
                            .passwordHash("google-oauth2")
                            .displayName(finalName)
                            .role(isAdminEmail && emailVerified ? Role.ADMIN : Role.CUSTOMER)
                            .totalScore(0L)
                            .build());
                });

        if (isAdminEmail && emailVerified && user.getRole() != Role.ADMIN) {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(UserPrincipal.from(user));
        String redirectUri = appProperties.getOauth2().getRedirectUri();
        if (redirectUri == null || redirectUri.isBlank()) {
            redirectUri = "http://localhost:5173/auth/callback";
        }
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isEmailVerified(Map<String, Object> attributes) {
        Object value = attributes.get("email_verified");
        if (value instanceof Boolean b) {
            return b;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }

    private boolean isAdminEmail(String email) {
        String configured = appProperties.getAdmin().getEmail();
        return configured != null && !configured.isBlank()
                && configured.trim().equalsIgnoreCase(email);
    }
}

