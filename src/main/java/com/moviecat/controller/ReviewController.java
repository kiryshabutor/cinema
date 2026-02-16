package com.moviecat.controller;

import com.moviecat.dto.ReviewDto;
import com.moviecat.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewDto> getAll() {
        return reviewService.getAll();
    }

    @GetMapping("/movie/{movieId}")
    public List<ReviewDto> getByMovieId(@PathVariable Long movieId) {
        return reviewService.getByMovieId(movieId);
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto dto) {
        return reviewService.create(dto);
    }
}
