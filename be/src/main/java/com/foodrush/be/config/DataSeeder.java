package com.foodrush.be.config;

import com.foodrush.be.entity.FoodItem;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.repository.FoodItemRepository;
import com.foodrush.be.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
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

    private static final List<FoodSeed> FOODS = List.of(
            new FoodSeed("BURGER", "Burger", "\uD83C\uDF54", 1),
            new FoodSeed("PIZZA", "Pizza", "\uD83C\uDF55", 1),
            new FoodSeed("FRIED_CHICKEN", "Fried Chicken", "\uD83C\uDF57", 1),
            new FoodSeed("FRENCH_FRIES", "French Fries", "\uD83C\uDF5F", 1),
            new FoodSeed("ICE_CREAM", "Ice Cream", "\uD83C\uDF66", 1),
            new FoodSeed("HOT_DOG", "Hot Dog", "\uD83C\uDF2D", 1),
            new FoodSeed("SANDWICH", "Sandwich", "\uD83E\uDD6A", 1),
            new FoodSeed("CHICKEN_RICE", "Chicken Rice", "\uD83C\uDF5A", 1),
            new FoodSeed("PASTA", "Pasta", "\uD83C\uDF5D", 1));

    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedFoodItems();
        if (appProperties.getSeed().isEnabled()) {
            seedStaff();
        }
    }

    private void seedFoodItems() {
        for (FoodSeed food : FOODS) {
            if (!foodItemRepository.existsByCode(food.code())) {
                foodItemRepository.save(FoodItem.builder()
                        .code(food.code())
                        .name(food.name())
                        .emoji(food.emoji())
                        .basePoints(food.basePoints())
                        .active(true)
                        .build());
            }
        }
    }

    private void seedStaff() {
        AppProperties.Seed seed = appProperties.getSeed();
        if (userRepository.existsByUsername(seed.getStaffUsername())) {
            return;
        }

        String password = seed.getStaffPassword();
        boolean generated = !StringUtils.hasText(password);
        if (generated) {
            password = generatePassword();
        }

        User staff = User.builder()
                .username(seed.getStaffUsername())
                .email(seed.getStaffEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .displayName(seed.getStaffDisplayName())
                .role(Role.STAFF)
                .totalScore(0L)
                .build();
        userRepository.save(staff);

        if (generated) {
            writeCredentials(seed.getStaffUsername(), password);
            log.warn("Seeded STAFF account '{}' with a generated password. "
                    + "Credentials written to '{}' - change it and delete the file.",
                    seed.getStaffUsername(), seed.getStaffPasswordFile());
        } else {
            log.info("Seeded STAFF account '{}'.", seed.getStaffUsername());
        }
    }

    private String generatePassword() {
        StringBuilder builder = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            builder.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
        }
        return builder.toString();
    }

    private void writeCredentials(String username, String password) {
        String content = "username=" + username + System.lineSeparator()
                + "password=" + password + System.lineSeparator();
        Path path = Path.of(appProperties.getSeed().getStaffPasswordFile());
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("Failed to write staff credentials to '{}': {}", path, ex.getMessage());
        }
    }

    private record FoodSeed(String code, String name, String emoji, int basePoints) {
    }
}
