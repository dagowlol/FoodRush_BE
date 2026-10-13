package com.foodrush.be.config;

import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int GENERATED_PASSWORD_LENGTH = 16;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appProperties.getSeed().isEnabled()) {
            seedStaff();
            seedAdmin();
        }
        promoteConfiguredAdmin();
    }

    private void seedStaff() {
        AppProperties.Seed seed = appProperties.getSeed();
        if (userRepository.existsByUsername(seed.getStaffUsername())) {
            return;
        }
        createAccount(seed.getStaffUsername(), seed.getStaffEmail(), seed.getStaffDisplayName(),
                seed.getStaffPassword(), Role.STAFF, seed.getStaffPasswordFile());
    }

    private void seedAdmin() {
        AppProperties.Admin admin = appProperties.getAdmin();
        String username = admin.getUsername();
        if (!StringUtils.hasText(username) || userRepository.existsByUsername(username)) {
            return;
        }
        // Không có email cấu hình thì dựng email nội bộ để tài khoản vẫn hợp lệ.
        String email = StringUtils.hasText(admin.getEmail())
                ? admin.getEmail().trim().toLowerCase(Locale.ROOT)
                : username + "@fivestar.local";
        createAccount(username, email, admin.getDisplayName(), admin.getPassword(),
                Role.ADMIN, admin.getPasswordFile());
    }

    /**
     * Tạo một tài khoản với mật khẩu do cấu hình chỉ định. Nếu không có mật khẩu thì
     * sinh ngẫu nhiên và ghi ra file để người đọc có thể đăng nhập lần đầu.
     */
    private void createAccount(String username, String email, String displayName,
                               String configuredPassword, Role role, String passwordFile) {
        boolean generated = !StringUtils.hasText(configuredPassword);
        String password = generated ? generatePassword() : configuredPassword;

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(StringUtils.hasText(displayName) ? displayName : username)
                .role(role)
                .totalScore(0L)
                .build();
        userRepository.save(user);

        if (generated) {
            writeCredentials(passwordFile, username, password);
            log.warn("Seeded {} account '{}' with a generated password. "
                            + "Credentials written to '{}' - change it and delete the file.",
                    role, username, passwordFile);
        } else {
            log.info("Seeded {} account '{}'.", role, username);
        }
    }

    /**
     * Tài khoản đăng ký sẵn (Google hoặc form đăng ký) sẽ được nâng lên ADMIN nếu
     * email của nó trùng với {@code ADMIN_EMAIL}.
     */
    private void promoteConfiguredAdmin() {
        String adminEmail = appProperties.getAdmin().getEmail();
        if (!StringUtils.hasText(adminEmail)) {
            return;
        }
        userRepository.findByEmail(adminEmail.trim().toLowerCase(Locale.ROOT)).ifPresent(user -> {
            if (user.getRole() != Role.ADMIN) {
                user.setRole(Role.ADMIN);
                userRepository.save(user);
                log.info("Granted ADMIN role to configured admin account '{}'.", adminEmail);
            }
        });
    }

    private String generatePassword() {
        StringBuilder builder = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            builder.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
        }
        return builder.toString();
    }

    private void writeCredentials(String passwordFile, String username, String password) {
        if (!StringUtils.hasText(passwordFile)) {
            log.warn("No password file configured for account '{}'. "
                    + "The generated password can not be read back.", username);
            return;
        }
        String content = "username=" + username + System.lineSeparator()
                + "password=" + password + System.lineSeparator();
        Path path = Path.of(passwordFile);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("Failed to write credentials to '{}': {}", path, ex.getMessage());
        }
    }
}