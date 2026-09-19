package com.sentinelx.common.response;

import java.time.Instant;

/**
 * Wrapper chuẩn cho mọi response THÀNH CÔNG.
 * Lỗi dùng ErrorResponse riêng (xem GlobalExceptionHandler).
 */
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) { //record trong java chỉ chứ dữ liệu (immutable data carrier) là kiểu dữ liệu bất biến

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}