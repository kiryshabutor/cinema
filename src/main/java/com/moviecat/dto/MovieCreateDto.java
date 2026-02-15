package com.moviecat.dto;

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
public class MovieCreateDto {

    @NotBlank(message = "Title is required")
    String title;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be no earlier than 1888")
    Integer year;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    Integer duration;

    @NotNull(message = "View count is required")
    @Min(value = 0, message = "View count must be non-negative")
    Long viewCount = 0L;

    Long directorId;
    Long studioId;
    Set<Long> genreIds;
    List<ReviewDto> reviews;
}
