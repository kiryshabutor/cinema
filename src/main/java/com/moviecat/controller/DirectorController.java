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

    private final DirectorRepository directorRepository;

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorRepository.findAll().stream()
                .map(d -> new DirectorDto(d.getId(), d.getFullName()))
                .toList();
    }

    @PostMapping
    public DirectorDto create(@jakarta.validation.Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setFullName(dto.getFullName());
        Director saved = directorRepository.save(director);
        return new DirectorDto(saved.getId(), saved.getFullName());
    }
}
