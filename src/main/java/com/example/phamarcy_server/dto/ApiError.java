package com.example.phamarcy_server.dto;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ApiViolation> violations
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError withViolations(int status, String error, String message, String path, List<ApiViolation> violations) {
        return new ApiError(Instant.now(), status, error, message, path, violations == null ? List.of() : violations);
    }
}