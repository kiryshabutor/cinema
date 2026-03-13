package com.moviecat.controller;

import com.moviecat.dto.GenreDto;
import com.moviecat.model.Genre;
import com.moviecat.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Genre management")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "Get paginated genres")
    public ResponseEntity<Page<GenreDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: id, name", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(genreService.getAll(page, size, sort, direction)
                .map(g -> new GenreDto(g.getId(), g.getName())));
    }

    @PostMapping
    @Operation(summary = "Create genre")
    public ResponseEntity<GenreDto> create(@Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre saved = genreService.create(genre);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenreDto(saved.getId(), saved.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update genre")
    public ResponseEntity<GenreDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody GenreDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.getName());
        Genre updated = genreService.update(id, genre);
        return ResponseEntity.ok(new GenreDto(updated.getId(), updated.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete genre")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
