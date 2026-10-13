package com.foodrush.be.dto.request.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterVerifyOtpRequest(

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email quá dài")
        String email,

        @NotBlank(message = "Vui lòng nhập mã OTP")
        @Size(min = 4, max = 10, message = "Mã OTP không hợp lệ")
        String otp,

        @NotBlank(message = "Vui lòng nhập tên đăng nhập")
        String username,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        String password,

        @Size(max = 120, message = "Tên hiển thị quá dài")
        String displayName) {
}
