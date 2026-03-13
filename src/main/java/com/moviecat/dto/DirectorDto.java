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
@Schema(description = "Director model")
public class DirectorDto {
    @Schema(description = "Director ID", example = "1")
    private Long id;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Nolan")
    private String lastName;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Christopher")
    private String firstName;

    @Schema(description = "Middle name")
    private String middleName;
}
