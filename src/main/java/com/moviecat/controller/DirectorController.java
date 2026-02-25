package com.moviecat.controller;

import com.moviecat.dto.DirectorDto;
import com.moviecat.model.Director;
import com.moviecat.service.DirectorService;
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
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDto>> getAll() {
        return ResponseEntity.ok(directorService.getAll().stream()
                .map(d -> new DirectorDto(d.getId(), d.getFullName()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<DirectorDto> create(@Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setFullName(dto.getFullName());
        Director saved = directorService.create(director);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DirectorDto(saved.getId(), saved.getFullName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectorDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setFullName(dto.getFullName());
        Director updated = directorService.update(id, director);
        return ResponseEntity.ok(new DirectorDto(updated.getId(), updated.getFullName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
