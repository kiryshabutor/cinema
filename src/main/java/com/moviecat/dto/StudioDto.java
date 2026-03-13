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
@Schema(description = "Studio model")
public class StudioDto {
    @Schema(description = "Studio ID", example = "1")
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Schema(description = "Studio title", example = "Warner Bros")
    private String title;
    
    @Schema(description = "Studio address", example = "Burbank, CA")
    private String address;
}
