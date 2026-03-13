package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Genre model")
public class GenreDto {
    @Schema(description = "Genre ID", example = "1")
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Genre name", example = "Sci-Fi")
    private String name;
}
