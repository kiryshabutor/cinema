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

    private final com.moviecat.service.GenreService genreService;

    @GetMapping
    public List<GenreDto> getAll() {
        return genreService.getAll().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList();
    }

    @PostMapping
    public GenreDto create(@Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre saved = genreService.create(genre);
        return new GenreDto(saved.getId(), saved.getName());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public GenreDto update(@org.springframework.web.bind.annotation.PathVariable Long id, @Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre updated = genreService.update(id, genre);
        return new GenreDto(updated.getId(), updated.getName());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        genreService.delete(id);
    }
}
