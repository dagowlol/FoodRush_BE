package com.foodrush.be.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Wrapper phân trang chuẩn cho các API dạng danh sách.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), page, size, 0, 0, true, true);
    }
}