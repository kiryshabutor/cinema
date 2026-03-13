package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compact genre item in movie response")
public class GenreItemDto {
    @Schema(description = "Genre ID", example = "1")
    private Long id;
    @Schema(description = "Genre name", example = "Sci-Fi")
    private String name;
}
