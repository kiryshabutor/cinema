package com.moviecat.controller;

import com.moviecat.dto.GenreDto;
import com.moviecat.model.Genre;
import com.moviecat.repository.GenreRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public List<GenreDto> getAll() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList();
    }

    @PostMapping
    public GenreDto create(@jakarta.validation.Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre saved = genreRepository.save(genre);
        return new GenreDto(saved.getId(), saved.getName());
    }
}
