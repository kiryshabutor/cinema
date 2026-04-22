package com.moviecat.controller;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieSearchParams;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.dto.PosterImportRequestDto;
import com.moviecat.dto.PosterScrapeRequestDto;
import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.dto.TaskStartResponseDto;
import com.moviecat.dto.ViewCountResponseDto;
import com.moviecat.dto.ViewRaceDemoResponseDto;
import com.moviecat.exception.response.ErrorResponse;
import com.moviecat.service.MovieService;
import com.moviecat.service.task.ReviewTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Movies", description = "Movie management and advanced search")
@ApiResponses(value = {
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class MovieController {

    private final MovieService movieService;
    private final ReviewTaskService reviewTaskService;

    @GetMapping
    @Operation(
            summary = "Get paginated movie list",
            description = "Returns a paginated movie list. Supports sorting and JPQL/native query mode.")
    public ResponseEntity<Page<MovieResponseDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @Min(value = 1, message = "Size must be between 1 and 100")
            @Max(value = 100, message = "Size must be between 1 and 100")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: title, year, viewCount, id", example = "title")
            @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Use native SQL query mode", example = "false")
            @RequestParam(name = "native", defaultValue = "false") boolean nativeQuery) {
        return ResponseEntity.ok(movieService.getAllPaged(page, size, sort, direction, nativeQuery));
    }

    @GetMapping("/nplus1-demo")
    @Operation(summary = "N+1 demo", description = "Returns movies via a method that intentionally demonstrates N+1.")
    public ResponseEntity<List<MovieResponseDto>> getAllNPlusOneDemo() {
        return ResponseEntity.ok(movieService.getAllNPlusOneDemo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by id")
    public ResponseEntity<MovieResponseDto> getById(@PathVariable @Positive long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Advanced movie search",
            description = "Search by title, director last name, director first name, genre name and studio title "
                    + "with pagination and sorting.")
    public ResponseEntity<Page<MovieResponseDto>> searchAdvanced(
            @Parameter(description = "Title filter (contains, case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Director last name filter (contains, case-insensitive)")
            @RequestParam(required = false) String directorLastName,
            @Parameter(description = "Director first name filter (contains, case-insensitive)")
            @RequestParam(required = false) String directorFirstName,
            @Parameter(description = "Genre name filter (contains, case-insensitive)")
            @RequestParam(required = false) String genreName,
            @Parameter(description = "Studio title filter (contains, case-insensitive)")
            @RequestParam(required = false) String studioTitle,
            @Parameter(description = "Page number (0-based)", example = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @Min(value = 1, message = "Size must be between 1 and 100")
            @Max(value = 100, message = "Size must be between 1 and 100")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: title, year, viewCount, id", example = "title")
            @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Use native SQL query mode", example = "false")
            @RequestParam(name = "native", defaultValue = "false") boolean nativeQuery) {
        return ResponseEntity.ok(movieService.searchAdvanced(new MovieSearchParams(
                title,
                directorLastName,
                directorFirstName,
                genreName,
                studioTitle,
                page,
                size,
                sort,
                direction,
                nativeQuery)));
    }

    @PostMapping
    @Operation(summary = "Create movie")
    public ResponseEntity<MovieResponseDto> create(@Valid @RequestBody MovieCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(dto));
    }

    @PostMapping("/{movieId}/reviews/async")
    @Operation(
            summary = "Start async bulk create reviews for movie",
            description = "Runs bulk review creation asynchronously and returns task ID for status checks.")
    public ResponseEntity<TaskStartResponseDto> createReviewsBulkAsync(
            @PathVariable @Positive long movieId,
            @Valid @RequestBody(required = false) List<@Valid ReviewCreateItemDto> reviews,
            @Parameter(description = "Simulate failure on the last review", example = "false")
            @RequestParam(defaultValue = "false") boolean fail,
            @Parameter(description = "Delay in seconds before task switches from CREATED to RUNNING", example = "0")
            @Min(value = 0, message = "startDelaySec must be greater than or equal to 0")
            @RequestParam(defaultValue = "0") int startDelaySec,
            @Parameter(description = "Delay in seconds before processing each review item", example = "0")
            @Min(value = 0, message = "itemDelaySec must be greater than or equal to 0")
            @RequestParam(defaultValue = "0") int itemDelaySec) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(reviewTaskService.startBulkCreateTask(movieId, reviews, fail, startDelaySec, itemDelaySec));
    }

    @PostMapping("/{id}/views")
    @Operation(summary = "Increment movie view counter by 1")
    public ResponseEntity<ViewCountResponseDto> incrementViewCount(@PathVariable @Positive long id) {
        return ResponseEntity.ok(movieService.incrementViewCount(id));
    }

    @PostMapping("/{id}/views/race-demo")
    @Operation(
            summary = "Run concurrent movie view increment demo",
            description = "Runs 50+ threads to demonstrate in-memory race condition and safe atomic mode.")
    public ResponseEntity<ViewRaceDemoResponseDto> runViewRaceDemo(
            @PathVariable @Positive long id,
            @Parameter(description = "Mode: unsafe or safe", example = "unsafe")
            @RequestParam String mode,
            @Parameter(description = "Number of concurrent threads (minimum 50)", example = "50")
            @Min(value = 50, message = "threads must be greater than or equal to 50")
            @RequestParam(defaultValue = "50") int threads,
            @Parameter(description = "Increments per thread (minimum 1)", example = "1000")
            @Min(value = 1, message = "incrementsPerThread must be greater than or equal to 1")
            @RequestParam(defaultValue = "1000") int incrementsPerThread) {
        if (threads < 50) {
            throw new IllegalArgumentException("threads must be greater than or equal to 50");
        }
        if (incrementsPerThread < 1) {
            throw new IllegalArgumentException("incrementsPerThread must be greater than or equal to 1");
        }
        return ResponseEntity.ok(movieService.runViewRaceDemo(id, mode, threads, incrementsPerThread));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update movie (full replace)")
    public ResponseEntity<MovieResponseDto> update(@PathVariable @Positive long id,
                                                   @Valid @RequestBody MovieUpdateDto dto) {
        return ResponseEntity.ok(movieService.update(id, dto));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch movie (partial update)")
    public ResponseEntity<MovieResponseDto> patch(@PathVariable @Positive long id,
                                                  @Valid @RequestBody MoviePatchDto dto) {
        return ResponseEntity.ok(movieService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete movie")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/poster")
    @Operation(summary = "Upload movie poster", description = "Uploads image and updates poster URL for the movie.")
    public ResponseEntity<Map<String, String>> uploadPoster(
            @PathVariable @Positive long id,
            @Parameter(description = "Image file (jpg/jpeg/png/webp/gif)")
            @RequestParam("file") MultipartFile file) {
        String fileUrl = movieService.uploadPoster(id, file);
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }

    @PostMapping("/{id}/poster/import")
    @Operation(summary = "Import movie poster from TMDB by poster path")
    public ResponseEntity<Map<String, String>> importPoster(
            @PathVariable @Positive long id,
            @Valid @RequestBody PosterImportRequestDto dto) {
        String fileUrl = movieService.importPosterFromTmdb(id, dto.getPosterPath());
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }

    @PostMapping("/{id}/poster/scrape")
    @Operation(summary = "Scrape movie poster via Wikipedia API by query and year")
    public ResponseEntity<Map<String, String>> scrapePoster(
            @PathVariable @Positive long id,
            @Valid @RequestBody PosterScrapeRequestDto dto) {
        String fileUrl = movieService.scrapePosterFromWikipedia(id, dto.getQuery(), dto.getYear());
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }
}
