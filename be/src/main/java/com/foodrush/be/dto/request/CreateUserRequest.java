package com.foodrush.be.dto.request;

import com.foodrush.be.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Payload dùng khi admin tạo tài khoản mới từ trang quản trị.
 */
public record CreateUserRequest(

        @NotBlank(message = "Vui lòng nhập tên đăng nhập")
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải có từ 3 đến 50 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Tên đăng nhập chỉ được chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang")
        String username,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email quá dài")
        String email,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 6, max = 72, message = "Mật khẩu phải có từ 6 đến 72 ký tự")
        String password,

        @Size(max = 120, message = "Tên hiển thị quá dài")
        String displayName,

        @NotNull(message = "Vui lòng chọn vai trò")
        Role role,

        @PositiveOrZero(message = "Số dư coin phải bằng 0 hoặc lớn hơn")
        Long coinBalance) {
}