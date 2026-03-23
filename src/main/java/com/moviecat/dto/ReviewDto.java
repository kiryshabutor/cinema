package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "ReviewPayload", description = "Review request/response payload")
public class ReviewDto extends ReviewContentDto {
    @Schema(
            description = "Review ID",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Movie ID is required")
    @Schema(
            description = "Movie ID",
            example = "1",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long movieId;

    public ReviewDto(Long id, String authorAlias, Integer rating, String comment, Long movieId) {
        super(authorAlias, rating, comment);
        this.id = id;
        this.movieId = movieId;
    }
}
