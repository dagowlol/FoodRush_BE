package com.foodrush.be.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.config.cloudinary.CloudinaryProperties;
import com.foodrush.be.dto.response.VoucherImageUploadResponse;
import com.foodrush.be.exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final CloudinaryProperties properties;

    public VoucherImageUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Vui lòng chọn tệp ảnh");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Dung lượng ảnh phải nhỏ hơn " + (properties.getMaxFileSizeBytes() / (1024 * 1024)) + " MB");
        }

        String detectedType;
        try (InputStream in = file.getInputStream()) {
            detectedType = detectImageType(in);
        } catch (IOException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Không đọc được tệp ảnh vừa tải lên");
        }
        if (detectedType == null || !properties.getAllowedContentTypes().contains(detectedType)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ chấp nhận ảnh định dạng JPEG, PNG hoặc WebP");
        }

        if (properties.getCloudName() == null || properties.getCloudName().isBlank()
                || properties.getApiKey() == null || properties.getApiKey().isBlank()
                || properties.getApiSecret() == null || properties.getApiSecret().isBlank()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cloudinary chưa được cấu hình");
        }

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.getCloudName(),
                "api_key", properties.getApiKey(),
                "api_secret", properties.getApiSecret(),
                "secure", true));

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", properties.getFolder(),
                    "resource_type", "image"));
            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            if (url == null || url.isBlank()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Cloudinary không trả về đường dẫn ảnh");
            }
            return new VoucherImageUploadResponse(url, publicId);
        } catch (Exception ex) {
            String detail = ex.getMessage() == null ? "" : (": " + ex.getMessage());
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Tải ảnh lên Cloudinary thất bại" + detail);
        }
    }

    private String detectImageType(InputStream in) throws IOException {
        byte[] header = in.readNBytes(16);
        if (header.length < 12) {
            return null;
        }
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
            return "image/png";
        }
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
