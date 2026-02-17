package com.moviecat.controller;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MovieDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieSearchCriteria;
import com.moviecat.service.MovieService;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<MovieDto>> getAll(
        @RequestParam(required = false, defaultValue = "eager") String fetchType) {
        return ResponseEntity.ok(movieService.getAll(fetchType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MovieDto>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String directorName,
            @RequestParam(required = false) String studioTitle,
            @RequestParam(required = false) String genreName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean useNative) {
        
        MovieSearchCriteria criteria = new MovieSearchCriteria(title, directorName, studioTitle, genreName, page, size);
        return ResponseEntity.ok(movieService.search(criteria, useNative));
    }

    @PostMapping
    public ResponseEntity<MovieDto> create(@Valid @RequestBody MovieCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(dto));
    }

    @PostMapping("/with-reviews")
    public ResponseEntity<MovieDto> createWithReviews(@Valid @RequestBody MovieCreateDto dto,
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
    public ResponseEntity<MovieDto> update(@PathVariable Long id, @Valid @RequestBody MovieDto dto) {
        return ResponseEntity.ok(movieService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovieDto> patch(@PathVariable Long id, @Valid @RequestBody MoviePatchDto dto) {
        return ResponseEntity.ok(movieService.patch(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/poster")
    public ResponseEntity<Map<String, String>> uploadPoster(
            @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) {
        String fileUrl = movieService.uploadPoster(id, file);
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }
}
