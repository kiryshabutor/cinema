package com.moviecat.controller;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.service.MovieService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getAll(
        @RequestParam(required = false, defaultValue = "eager") String fetchType) {
        return ResponseEntity.ok(movieService.getAll(fetchType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchByTitle(title));
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> create(@Valid @RequestBody MovieCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(dto));
    }

    @PostMapping("/with-reviews")
    public ResponseEntity<MovieResponseDto> createWithReviews(@Valid @RequestBody MovieCreateDto dto,
                                      @RequestParam(defaultValue = "false") boolean fail,
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
    public ResponseEntity<MovieResponseDto> update(@PathVariable @NonNull Long id,
                                                   @Valid @RequestBody MovieUpdateDto dto) {
        return ResponseEntity.ok(movieService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovieResponseDto> patch(@PathVariable @NonNull Long id,
                                                  @Valid @RequestBody MoviePatchDto dto) {
        return ResponseEntity.ok(movieService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/poster")
    public ResponseEntity<Map<String, String>> uploadPoster(
            @PathVariable @NonNull Long id,
            @RequestParam("file") MultipartFile file) {
        String fileUrl = movieService.uploadPoster(id, file);
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }
}
