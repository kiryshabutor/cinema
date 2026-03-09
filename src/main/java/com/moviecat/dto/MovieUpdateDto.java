package com.moviecat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MovieUpdateDto {

    @NotBlank(message = "Title is required")
    String title;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be no earlier than 1888")
    @Max(value = 2027, message = "Year must be no later than 2027")
    Integer year;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    Integer duration;

    @NotNull(message = "View count is required")
    @Min(value = 0, message = "View count must be non-negative")
    Long viewCount;

    Long directorId;
    Long studioId;
    Set<Long> genreIds;
}
