package com.moviecat.controller;

import com.moviecat.dto.DirectorDto;
import com.moviecat.model.Director;
import com.moviecat.service.DirectorService;
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
@RequestMapping("/api/directors")
@RequiredArgsConstructor
@Tag(name = "Directors", description = "Director management")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    @Operation(summary = "Get paginated directors")
    public ResponseEntity<Page<DirectorDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: id, lastName, firstName, middleName", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(directorService.getAll(page, size, sort, direction)
                .map(d -> new DirectorDto(d.getId(), d.getLastName(), d.getFirstName(), d.getMiddleName())));
    }

    @PostMapping
    @Operation(summary = "Create director")
    public ResponseEntity<DirectorDto> create(@Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setLastName(dto.getLastName());
        director.setFirstName(dto.getFirstName());
        director.setMiddleName(dto.getMiddleName());
        Director saved = directorService.create(director);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DirectorDto(saved.getId(), saved.getLastName(), saved.getFirstName(), saved.getMiddleName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update director")
    public ResponseEntity<DirectorDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody DirectorDto dto) {
        Director director = new Director();
        director.setLastName(dto.getLastName());
        director.setFirstName(dto.getFirstName());
        director.setMiddleName(dto.getMiddleName());
        Director updated = directorService.update(id, director);
        return ResponseEntity.ok(
                new DirectorDto(
                        updated.getId(), updated.getLastName(), updated.getFirstName(), updated.getMiddleName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete director")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
