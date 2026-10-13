package com.foodrush.be.auth.oauth;

import com.foodrush.be.config.AppProperties;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String sub = (String) attributes.get("sub");

        if (!StringUtils.hasText(email)) {
            email = sub + "@google.local";
        }

        String finalEmail = email.toLowerCase();
        String finalName = StringUtils.hasText(name) ? name : email.split("@")[0];

        boolean emailVerified = isEmailVerified(attributes);
        boolean isAdminEmail = isAdminEmail(finalEmail);

        Optional<User> existing = userRepository.findByEmail(finalEmail);
        User user = existing.orElseGet(() -> {
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

        if (existing.isPresent() && isAdminEmail && emailVerified && user.getRole() != Role.ADMIN) {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }

        String attrName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                attrName);
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

