package com.foodrush.be.controller;

import com.foodrush.be.common.ApiResponse;
import com.foodrush.be.dto.request.VoucherCreateRequest;
import com.foodrush.be.dto.request.VoucherUpdateRequest;
import com.foodrush.be.dto.response.IssuedVoucherResponse;
import com.foodrush.be.dto.response.VoucherResponse;
import com.foodrush.be.security.SecurityUtils;
import com.foodrush.be.dto.response.VoucherImageUploadResponse;
import com.foodrush.be.service.AdminVoucherService;
import com.foodrush.be.service.CloudinaryUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Vouchers")
@RestController
@RequestMapping("/api/admin/vouchers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherService adminVoucherService;
    private final CloudinaryUploadService cloudinaryUploadService;

    @Operation(summary = "List all voucher types")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(adminVoucherService.list()));
    }

    @Operation(summary = "Get one voucher type")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminVoucherService.get(id)));
    }

    @Operation(summary = "Create a new voucher type")
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherCreateRequest request) {
        VoucherResponse response = adminVoucherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Đã tạo voucher", response));
    }

    @Operation(summary = "Update a voucher type")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VoucherUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật voucher", adminVoucherService.update(id, request)));
    }

    @Operation(summary = "Enable or disable a voucher")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VoucherResponse>> status(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean active = body == null ? null : body.get("active");
        if (active == null) {
            throw new com.foodrush.be.exception.AppException(com.foodrush.be.common.ErrorCode.VALIDATION_ERROR);
        }
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật trạng thái voucher",
                adminVoucherService.updateStatus(id, active)));
    }

    @Operation(summary = "List issued vouchers")
    @GetMapping("/issued")
    public ResponseEntity<ApiResponse<List<IssuedVoucherResponse>>> issued() {
        return ResponseEntity.ok(ApiResponse.ok(adminVoucherService.issuedVouchers()));
    }

    @Operation(summary = "Upload a voucher image to Cloudinary")
    @PostMapping(value = "/upload-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VoucherImageUploadResponse>> uploadImage(
            @org.springframework.web.bind.annotation.RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        VoucherImageUploadResponse response = cloudinaryUploadService.upload(image);
        return ResponseEntity.ok(ApiResponse.ok("Đã tải ảnh lên", response));
    }
}