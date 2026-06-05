package com.motointel.app.common;

import java.util.List;

/** Error payload (§4): {@code { code, message, details:[...] }}. */
public record ApiError(String code, String message, List<Object> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(String code, String message, List<Object> details) {
        return new ApiError(code, message, details == null ? List.of() : details);
    }
}
