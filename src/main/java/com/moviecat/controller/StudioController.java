package com.moviecat.controller;

import com.moviecat.dto.StudioDto;
import com.moviecat.model.Studio;
import com.moviecat.service.StudioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @GetMapping
    public ResponseEntity<List<StudioDto>> getAll() {
        return ResponseEntity.ok(studioService.getAll().stream()
                .map(s -> new StudioDto(s.getId(), s.getTitle(), s.getAddress()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<StudioDto> create(@Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio saved = studioService.create(studio);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StudioDto(saved.getId(), saved.getTitle(), saved.getAddress()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudioDto> update(@PathVariable Long id, @Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio updated = studioService.update(id, studio);
        return ResponseEntity.ok(new StudioDto(updated.getId(), updated.getTitle(), updated.getAddress()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
