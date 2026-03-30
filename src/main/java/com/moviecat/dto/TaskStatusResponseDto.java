package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Async task status details")
public record TaskStatusResponseDto(
        @Schema(description = "Task ID", example = "1")
        Long taskId,
        @Schema(description = "Current status", example = "RUNNING")
        TaskExecutionStatus status,
        @Schema(description = "Task creation timestamp", example = "2026-03-30T19:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Task start timestamp", example = "2026-03-30T19:00:02")
        LocalDateTime startedAt,
        @Schema(description = "Task finish timestamp", example = "2026-03-30T19:00:04")
        LocalDateTime finishedAt,
        @Schema(description = "How many items are planned for processing", example = "10")
        int totalCount,
        @Schema(description = "How many items are already processed", example = "4")
        int processedCount,
        @Schema(description = "Failure reason if task is FAILED", nullable = true)
        String errorMessage
) {
}
