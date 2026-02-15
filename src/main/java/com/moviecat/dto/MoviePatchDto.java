package com.moviecat.dto;

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
public class MoviePatchDto {

    String title;

    @Min(value = 1888, message = "Year must be no earlier than 1888")
    Integer year;

    @Min(value = 1, message = "Duration must be positive")
    Integer duration;

    @Min(value = 0, message = "View count must be non-negative")
    Long viewCount;

    Long directorId;
    Long studioId;
    Set<Long> genreIds;
}
