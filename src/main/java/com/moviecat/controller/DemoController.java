package com.moviecat.controller;

import com.moviecat.model.Movie;
import com.moviecat.repository.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final MovieRepository movieRepository;

    @GetMapping("/n-plus-one")
    public List<String> demonstrateNPlusOne() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(m -> m.getTitle() + " has " + m.getGenres().size() + " genres")
                .toList();
    }

    @GetMapping("/join-fetch")
    public List<String> demonstrateJoinFetch() {
        List<Movie> movies = movieRepository.findAllWithGenres();
        return movies.stream()
                .map(m -> m.getTitle() + " has " + m.getGenres().size() + " genres")
                .toList();
    }
}
