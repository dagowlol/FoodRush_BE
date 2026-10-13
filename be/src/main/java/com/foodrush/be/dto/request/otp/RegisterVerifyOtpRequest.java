package com.foodrush.be.dto.request.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterVerifyOtpRequest(

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        @Size(max = 120, message = "email is too long")
        String email,

        @NotBlank(message = "otp is required")
        @Size(min = 4, max = 10, message = "otp must be valid")
        String otp,

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "password is required")
        String password,

        @Size(max = 120, message = "displayName is too long")
        String displayName) {
}
