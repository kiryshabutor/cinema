package com.moviecat.controller;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MovieDto;
import com.moviecat.service.MovieService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<MovieDto> getAll(@RequestParam(required = false, defaultValue = "eager") String fetchType) {
        return movieService.getAll(fetchType);
    }

    @GetMapping("/{id}")
    public MovieDto getById(@PathVariable Long id) {
        return movieService.getById(id);
    }

    @GetMapping("/search")
    public List<MovieDto> searchByTitle(@RequestParam String title) {
        return movieService.searchByTitle(title);
    }

    @PostMapping
    public MovieDto create(@Valid @RequestBody MovieCreateDto dto) {
        return movieService.create(dto);
    }

    @PostMapping("/with-reviews")
    public MovieDto createWithReviews(@Valid @RequestBody MovieCreateDto dto, 
                                      @RequestParam(defaultValue = "false") boolean fail,
                                      @RequestParam(defaultValue = "true") boolean transactional) {
        if (transactional) {
            return movieService.createWithReviewsTransactional(dto, fail);
        } else {
            return movieService.createWithReviewsNonTransactional(dto, fail);
        }
    }

    @PutMapping("/{id}")
    public MovieDto update(@PathVariable Long id, @Valid @RequestBody MovieDto dto) {
        return movieService.update(id, dto);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}")
    public MovieDto patch(@PathVariable Long id, @Valid @RequestBody com.moviecat.dto.MoviePatchDto dto) {
        return movieService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
    }

    @PostMapping("/{id}/poster")
    public Map<String, String> uploadPoster(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String fileUrl = movieService.uploadPoster(id, file);
        return Collections.singletonMap("url", fileUrl);
    }
}
