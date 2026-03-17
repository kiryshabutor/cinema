package com.moviecat.exception.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Unified error response")
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "Error message", example = "Resource not found")
        String message,
        @Schema(description = "Timestamp", example = "2025-01-01T12:00:00")
        LocalDateTime timestamp,
        @Schema(description = "Field-level validation errors, present for validation failures", nullable = true)
        Map<String, String> errors
) {

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this(status, message, timestamp, null);
    }
}
