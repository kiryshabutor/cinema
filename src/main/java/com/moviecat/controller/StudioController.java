package com.moviecat.controller;

import com.moviecat.dto.StudioDto;
import com.moviecat.model.Studio;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final com.moviecat.service.StudioService studioService;

    @GetMapping
    public List<StudioDto> getAll() {
        return studioService.getAll().stream()
                .map(s -> new StudioDto(s.getId(), s.getTitle(), s.getAddress()))
                .toList();
    }

    @PostMapping
    public StudioDto create(@Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio saved = studioService.create(studio);
        return new StudioDto(saved.getId(), saved.getTitle(), saved.getAddress());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public StudioDto update(@org.springframework.web.bind.annotation.PathVariable Long id,
                            @Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio updated = studioService.update(id, studio);
        return new StudioDto(updated.getId(), updated.getTitle(), updated.getAddress());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        studioService.delete(id);
    }
}
