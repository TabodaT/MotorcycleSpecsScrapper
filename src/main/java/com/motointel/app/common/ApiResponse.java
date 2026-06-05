package com.motointel.app.common;

/**
 * Canonical response envelope (§4). All endpoints use this except /api/health.
 * {@code meta} is non-null only for paginated list responses.
 */
public record ApiResponse<T>(boolean success, T data, ApiError error, PageMeta meta) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error, null);
    }
}
