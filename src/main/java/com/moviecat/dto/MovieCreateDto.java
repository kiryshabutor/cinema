package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(name = "MovieCreateRequest", description = "Movie creation payload")
public class MovieCreateDto {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title length must be between 1 and 255")
    @Schema(
            description = "Movie title",
            example = "Interstellar",
            minLength = 1,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED)
    String title;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be no earlier than 1888")
    @Schema(description = "Release year", example = "2014", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer year;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    @Schema(description = "Duration in minutes", example = "169", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer duration;

    @NotNull(message = "View count is required")
    @Min(value = 0, message = "View count must be non-negative")
    @Schema(description = "Initial view count", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    Long viewCount = 0L;

    @Schema(description = "Director ID", example = "1")
    Long directorId;
    @Schema(description = "Studio ID", example = "1")
    Long studioId;
    @Schema(description = "Genre IDs", example = "[1, 4, 9]")
    Set<Long> genreIds;
    @Schema(description = "Optional reviews to create together with movie")
    @Valid
    List<@Valid ReviewDto> reviews;
}
