package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Response returned when async task is started")
public record TaskStartResponseDto(
        @Schema(description = "Task ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID taskId,
        @Schema(description = "Current status", example = "CREATED")
        TaskExecutionStatus status
) {
}
