package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when async task is started")
public record TaskStartResponseDto(
        @Schema(description = "Task ID", example = "1")
        Long taskId,
        @Schema(description = "Current status", example = "CREATED")
        TaskExecutionStatus status
) {
}
