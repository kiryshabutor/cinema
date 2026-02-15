package com.moviecat.controller;

import com.moviecat.dto.DirectorDto;
import com.moviecat.model.Director;
import com.moviecat.repository.DirectorRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final com.moviecat.service.DirectorService directorService;

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorService.getAll().stream()
                .map(d -> new DirectorDto(d.getId(), d.getFullName()))
                .toList();
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setFullName(dto.getFullName());
        Director saved = directorService.create(director);
        return new DirectorDto(saved.getId(), saved.getFullName());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public DirectorDto update(@org.springframework.web.bind.annotation.PathVariable Long id, @Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setFullName(dto.getFullName());
        Director updated = directorService.update(id, director);
        return new DirectorDto(updated.getId(), updated.getFullName());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        directorService.delete(id);
    }
}
