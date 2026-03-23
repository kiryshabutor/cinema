package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 255, message = "Title length must be between 1 and 255")
    @Pattern(regexp = ".*\\S.*", message = "Title must not be blank")
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

    @Positive(message = "Director ID must be positive")
    @Schema(description = "Director ID", example = "1", minimum = "1")
    Long directorId;
    @Positive(message = "Studio ID must be positive")
    @Schema(description = "Studio ID", example = "1", minimum = "1")
    Long studioId;
    @Schema(description = "Genre IDs", example = "[1, 4, 9]")
    Set<@Positive(message = "Genre ID must be positive") Long> genreIds;
}
