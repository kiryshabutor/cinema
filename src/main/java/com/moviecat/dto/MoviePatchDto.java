package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(name = "MoviePatchRequest", description = "Movie partial-update payload")
public class MoviePatchDto {

    @Schema(
            description = "Movie title",
            example = "Interstellar",
            minLength = 1,
            maxLength = 255)
    String title;

    @Min(value = 1888, message = "Year must be no earlier than 1888")
    @Max(value = 2027, message = "Year must be no later than 2027")
    @Schema(description = "Release year", example = "2014", minimum = "1888", maximum = "2027")
    Integer year;

    @Min(value = 1, message = "Duration must be positive")
    @Schema(description = "Duration in minutes", example = "169", minimum = "1")
    Integer duration;

    @Min(value = 0, message = "View count must be non-negative")
    @Schema(description = "View count", example = "100", minimum = "0")
    Long viewCount;

    @Schema(description = "Director ID", example = "1", minimum = "1")
    Long directorId;
    @Schema(description = "Studio ID", example = "1", minimum = "1")
    Long studioId;
    @Schema(description = "Genre IDs", example = "[1, 4, 9]")
    Set<Long> genreIds;
}
