package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Movie view count response")
public record ViewCountResponseDto(
        @Schema(description = "Movie ID", example = "113")
        Long movieId,
        @Schema(description = "Current movie view count", example = "101")
        Long viewCount
) {
}
