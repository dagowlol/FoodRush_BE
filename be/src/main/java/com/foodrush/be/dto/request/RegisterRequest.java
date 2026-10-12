package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "username may only contain letters, digits, dot, underscore or dash")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        @Size(max = 120, message = "email is too long")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 6, max = 72, message = "password must be between 6 and 72 characters")
        String password,

        @Size(max = 120, message = "displayName is too long")
        String displayName) {
}
