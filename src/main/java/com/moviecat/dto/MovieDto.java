package com.moviecat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {  

    private Long id;
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be no earlier than 1888")
    private Integer year;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    private Integer duration;

    @NotNull(message = "View count is required")
    @Min(value = 0, message = "View count must be non-negative")
    private Long viewCount;
}
