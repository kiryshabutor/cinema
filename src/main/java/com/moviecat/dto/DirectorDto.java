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
@Schema(name = "DirectorPayload", description = "Director request/response payload")
public class DirectorDto {
    @Schema(
            description = "Director ID",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Last name is required")
    @Schema(
            description = "Director last name",
            example = "Nolan",
            minLength = 1,
            maxLength = 120,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "First name is required")
    @Schema(
            description = "Director first name",
            example = "Christopher",
            minLength = 1,
            maxLength = 120,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Schema(description = "Director middle name", example = "Edward", maxLength = 120)
    private String middleName;
}
