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
@Schema(name = "TmdbPosterCandidate", description = "TMDB poster search candidate")
public class TmdbPosterCandidateDto {

    @Schema(description = "TMDB movie id", example = "157336")
    private Long tmdbId;
    @Schema(description = "Movie title", example = "Interstellar")
    private String title;
    @Schema(description = "Release year", example = "2014")
    private Integer releaseYear;
    @Schema(description = "Poster path for import", example = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg")
    private String posterPath;
    @Schema(description = "Preview URL", example = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg")
    private String previewUrl;
}
