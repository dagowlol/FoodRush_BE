package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.common.PageResponse;
import com.foodrush.be.dto.request.CreateUserRequest;
import com.foodrush.be.dto.request.UpdateUserRequest;
import com.foodrush.be.dto.response.AdminUserResponse;
import com.foodrush.be.dto.response.AdminUserStatsResponse;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Users")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List users with pagination, optionally filtered by keyword or role")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.list(keyword, role, page, size)));
    }

    @Operation(summary = "Overall user counts for the admin dashboard")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminUserStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.stats()));
    }

    @Operation(summary = "Create a user")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đã tạo người dùng", adminUserService.create(request)));
    }

    @Operation(summary = "Get one user")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.get(id)));
    }

    @Operation(summary = "Update a user's role or coin balance")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật người dùng", adminUserService.update(id, request)));
    }
}