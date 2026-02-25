package com.moviecat.controller;

import com.moviecat.dto.GenreDto;
import com.moviecat.model.Genre;
import com.moviecat.service.GenreService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDto>> getAll() {
        return ResponseEntity.ok(genreService.getAll().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<GenreDto> create(@Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre saved = genreService.create(genre);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenreDto(saved.getId(), saved.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre updated = genreService.update(id, genre);
        return ResponseEntity.ok(new GenreDto(updated.getId(), updated.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
