package com.moviecat.controller;

import com.moviecat.dto.DirectorDto;
import com.moviecat.exception.response.ErrorResponse;
import com.moviecat.model.Director;
import com.moviecat.service.DirectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/directors")
@RequiredArgsConstructor
@Validated
@Tag(name = "Directors", description = "Director management")
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
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    @Operation(summary = "Get paginated directors")
    @ApiResponse(responseCode = "200", description = "Directors retrieved successfully")
    public ResponseEntity<Page<DirectorDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, max 100)", example = "10")
            @Min(value = 1, message = "Size must be between 1 and 100")
            @Max(value = 100, message = "Size must be between 1 and 100")
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
    @ApiResponse(
            responseCode = "201",
            description = "Director created successfully",
            content = @Content(schema = @Schema(implementation = DirectorDto.class)))
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
    @ApiResponse(
            responseCode = "200",
            description = "Director updated successfully",
            content = @Content(schema = @Schema(implementation = DirectorDto.class)))
    public ResponseEntity<DirectorDto> update(@PathVariable @Positive long id, @Valid @RequestBody DirectorDto dto) {
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
    @ApiResponse(responseCode = "204", description = "Director deleted successfully")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
