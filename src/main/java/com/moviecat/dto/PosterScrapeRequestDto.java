package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "PosterScrapeRequest", description = "Wikipedia poster scraping payload")
public class PosterScrapeRequestDto {

    @NotBlank(message = "query is required")
    @Size(min = 1, max = 255, message = "query length must be between 1 and 255")
    @Schema(description = "Movie title query", example = "Interstellar")
    String query;

    @Min(value = 1888, message = "year must be no earlier than 1888")
    @Max(value = 2100, message = "year must be no later than 2100")
    @Schema(description = "Optional release year", example = "2014")
    Integer year;
}
