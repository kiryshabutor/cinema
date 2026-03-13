package com.moviecat.controller;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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
@Tag(name = "Movies", description = "Movie management and advanced search")
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    @Operation(
            summary = "Get paginated movie list",
            description = "Returns a paginated movie list. Supports sorting and JPQL/native query mode.")
    public ResponseEntity<Page<MovieResponseDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
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
    public ResponseEntity<MovieResponseDto> getById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Advanced movie search",
            description = "Search by title, director last name, genre name and studio title "
                    + "with pagination and sorting.")
    public ResponseEntity<Page<MovieResponseDto>> searchAdvanced(
            @Parameter(description = "Title filter (contains, case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Director last name filter (contains, case-insensitive)")
            @RequestParam(required = false) String directorLastName,
            @Parameter(description = "Genre name filter (contains, case-insensitive)")
            @RequestParam(required = false) String genreName,
            @Parameter(description = "Studio title filter (contains, case-insensitive)")
            @RequestParam(required = false) String studioTitle,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: title, year, viewCount, id", example = "title")
            @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Use native SQL query mode", example = "false")
            @RequestParam(name = "native", defaultValue = "false") boolean nativeQuery) {
        return ResponseEntity.ok(movieService.searchAdvanced(
                title,
                directorLastName,
                genreName,
                studioTitle,
                page,
                size,
                sort,
                direction,
                nativeQuery));
    }

    @PostMapping
    @Operation(summary = "Create movie")
    public ResponseEntity<MovieResponseDto> create(@Valid @RequestBody MovieCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(dto));
    }

    @PostMapping("/with-reviews")
    @Operation(
            summary = "Create movie with reviews",
            description = "Can simulate transactional/non-transactional failure.")
    public ResponseEntity<MovieResponseDto> createWithReviews(@Valid @RequestBody MovieCreateDto dto,
            @Parameter(description = "Simulate failure on the last review", example = "false")
            @RequestParam(defaultValue = "false") boolean fail,
            @Parameter(description = "Run operation in transaction", example = "true")
            @RequestParam(defaultValue = "true") boolean transactional) {
        if (transactional) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(movieService.createWithReviewsTransactional(dto, fail));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(movieService.createWithReviewsNonTransactional(dto, fail));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update movie (full replace)")
    public ResponseEntity<MovieResponseDto> update(@PathVariable @NonNull Long id,
                                                   @Valid @RequestBody MovieUpdateDto dto) {
        return ResponseEntity.ok(movieService.update(id, dto));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch movie (partial update)")
    public ResponseEntity<MovieResponseDto> patch(@PathVariable @NonNull Long id,
                                                  @Valid @RequestBody MoviePatchDto dto) {
        return ResponseEntity.ok(movieService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete movie")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/poster")
    @Operation(summary = "Upload movie poster", description = "Uploads image and updates poster URL for the movie.")
    public ResponseEntity<Map<String, String>> uploadPoster(
            @PathVariable @NonNull Long id,
            @Parameter(description = "Image file (jpg/jpeg/png/webp/gif)")
            @RequestParam("file") MultipartFile file) {
        String fileUrl = movieService.uploadPoster(id, file);
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }
}
