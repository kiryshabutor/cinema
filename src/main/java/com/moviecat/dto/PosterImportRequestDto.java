package com.moviecat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "PosterImportRequest", description = "TMDB poster import payload")
public class PosterImportRequestDto {

    @NotBlank(message = "posterPath is required")
    @Pattern(regexp = "^/.*", message = "posterPath must start with '/'")
    @Schema(description = "TMDB poster path", example = "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg")
    String posterPath;
}
