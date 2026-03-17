package com.moviecat.controller;

import com.moviecat.dto.StudioDto;
import com.moviecat.exception.response.ErrorResponse;
import com.moviecat.model.Studio;
import com.moviecat.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/studios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Studios", description = "Studio management")
@ApiResponses(value = {
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class StudioController {

    private final StudioService studioService;

    @GetMapping
    @Operation(summary = "Get paginated studios")
    public ResponseEntity<Page<StudioDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: id, title, address", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(studioService.getAll(page, size, sort, direction)
                .map(s -> new StudioDto(s.getId(), s.getTitle(), s.getAddress())));
    }

    @PostMapping
    @Operation(summary = "Create studio")
    public ResponseEntity<StudioDto> create(@Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio saved = studioService.create(studio);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StudioDto(saved.getId(), saved.getTitle(), saved.getAddress()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update studio")
    public ResponseEntity<StudioDto> update(@PathVariable @Positive long id, @Valid @RequestBody StudioDto dto) {
        Studio studio = new Studio();
        studio.setTitle(dto.getTitle());
        studio.setAddress(dto.getAddress());
        Studio updated = studioService.update(id, studio);
        return ResponseEntity.ok(new StudioDto(updated.getId(), updated.getTitle(), updated.getAddress()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete studio")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        studioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
