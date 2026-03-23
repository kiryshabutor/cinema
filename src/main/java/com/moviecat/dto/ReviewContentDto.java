package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewContentDto {

    @NotBlank(message = "Author alias is required")
    @Size(max = 80, message = "Author alias length must be at most 80")
    @Schema(
            description = "Author alias",
            example = "Critic42",
            minLength = 1,
            maxLength = 80,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorAlias;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 10")
    @Max(value = 10, message = "Rating must be between 1 and 10")
    @Schema(
            description = "Rating from 1 to 10",
            example = "9",
            minimum = "1",
            maximum = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Size(max = 500, message = "Comment length must be at most 500")
    @Schema(description = "Review comment", example = "Great movie!", maxLength = 500)
    private String comment;
}
