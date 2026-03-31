package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Concurrent movie view increment demo response")
public record ViewRaceDemoResponseDto(
        @Schema(description = "Movie ID", example = "113")
        Long movieId,
        @Schema(description = "Demo mode", example = "safe")
        String mode,
        @Schema(description = "Number of concurrent threads", example = "50")
        int threads,
        @Schema(description = "Number of increments per thread", example = "1000")
        int incrementsPerThread,
        @Schema(description = "Expected number of increments", example = "50000")
        long expectedCount,
        @Schema(description = "Actual number of increments observed", example = "50000")
        long actualCount,
        @Schema(description = "Lost updates due to race conditions", example = "0")
        long lostUpdates,
        @Schema(description = "Execution duration in milliseconds", example = "127")
        long durationMs
) {
}
