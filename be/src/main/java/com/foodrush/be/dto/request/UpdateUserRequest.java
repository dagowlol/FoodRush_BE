package com.foodrush.be.dto.request;

import com.foodrush.be.entity.enums.Role;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payload for editing a user from the admin portal. Null fields are left
 * untouched so the same DTO can serve partial updates.
 */
public record UpdateUserRequest(
        Role role,
        @PositiveOrZero(message = "Số dư coin phải bằng 0 hoặc lớn hơn")
        Long coinBalance,
        Boolean active) {
}