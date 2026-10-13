package com.foodrush.be.dto.response;

/**
 * Số liệu tổng quan cho trang quản trị người dùng, không phụ thuộc phân trang
 * hay bộ lọc đang dùng.
 */
public record AdminUserStatsResponse(
        long totalUsers,
        long lockedUsers,
        long staffUsers,
        long totalCoins) {
}