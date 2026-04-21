package com.moviecat.controller;

import com.moviecat.dto.TmdbPosterCandidateDto;
import com.moviecat.exception.response.ErrorResponse;
import com.moviecat.service.TmdbPosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posters")
@RequiredArgsConstructor
@Validated
@Tag(name = "Posters", description = "Poster search and import integration")
@ApiResponses(value = {
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "503",
            description = "External integration is not configured",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "502",
            description = "Upstream service error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class PosterController {

    private final TmdbPosterService tmdbPosterService;

    @GetMapping("/tmdb/search")
    @Operation(summary = "Search movie posters in TMDB")
    public ResponseEntity<List<TmdbPosterCandidateDto>> searchTmdbPosters(
            @Parameter(description = "Movie title query", example = "Interstellar")
            @RequestParam
            @Size(min = 1, max = 255, message = "query length must be between 1 and 255")
            String query,
            @Parameter(description = "Optional release year filter", example = "2014")
            @RequestParam(required = false)
            @Min(value = 1888, message = "year must be no earlier than 1888")
            @Max(value = 2100, message = "year must be no later than 2100")
            Integer year) {
        return ResponseEntity.ok(tmdbPosterService.searchPosters(query, year));
    }
}
