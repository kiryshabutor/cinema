package com.moviecat.controller;

import com.moviecat.dto.MovieDto;
import com.moviecat.service.MovieService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public List<MovieDto> getAll() {
        return movieService.getAll();
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
    public MovieDto create(@Valid @RequestBody MovieDto dto) {
        return movieService.create(dto);
    }
}
