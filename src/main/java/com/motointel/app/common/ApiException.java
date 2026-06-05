package com.motointel.app.common;

import org.springframework.http.HttpStatus;

import java.util.List;

/** Application-level exception carrying an error code, HTTP status, and optional details. */
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final transient List<Object> details;

    public ApiException(String code, HttpStatus status, String message, List<Object> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details == null ? List.of() : details;
    }

    public static ApiException notFound(String message) {
        return new ApiException("NOT_FOUND", HttpStatus.NOT_FOUND, message, List.of());
    }

    public static ApiException validation(String message, List<Object> details) {
        return new ApiException("VALIDATION", HttpStatus.BAD_REQUEST, message, details);
    }

    public static ApiException conflict(String message) {
        return new ApiException("CONFLICT", HttpStatus.CONFLICT, message, List.of());
    }

    public static ApiException unprocessable(String message) {
        return new ApiException("UNPROCESSABLE", HttpStatus.UNPROCESSABLE_ENTITY, message, List.of());
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
    public List<Object> getDetails() { return details; }
}
