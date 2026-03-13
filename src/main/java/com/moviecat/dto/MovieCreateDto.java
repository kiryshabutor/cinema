package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Payload for creating a movie")
public class MovieCreateDto {

    @NotBlank(message = "Title is required")
    @Schema(description = "Movie title", example = "Interstellar")
    String title;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be no earlier than 1888")
    @Schema(description = "Release year", example = "2014")
    Integer year;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    @Schema(description = "Duration in minutes", example = "169")
    Integer duration;

    @NotNull(message = "View count is required")
    @Min(value = 0, message = "View count must be non-negative")
    @Schema(description = "Initial view count", example = "0")
    Long viewCount = 0L;

    @Schema(description = "Director ID", example = "1")
    Long directorId;
    @Schema(description = "Studio ID", example = "1")
    Long studioId;
    @Schema(description = "Genre IDs", example = "[1, 4, 9]")
    Set<Long> genreIds;
    @Schema(description = "Optional reviews to create together with movie")
    List<ReviewDto> reviews;
}
