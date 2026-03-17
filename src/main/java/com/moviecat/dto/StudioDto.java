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
@Schema(name = "StudioPayload", description = "Studio request/response payload")
public class StudioDto {
    @Schema(
            description = "Studio ID",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Schema(
            description = "Studio title",
            example = "Warner Bros",
            minLength = 1,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Studio address", example = "Burbank, CA", maxLength = 255)
    private String address;
}
