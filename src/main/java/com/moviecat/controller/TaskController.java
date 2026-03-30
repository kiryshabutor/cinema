package com.moviecat.controller;

import com.moviecat.dto.TaskStatusResponseDto;
import com.moviecat.exception.response.ErrorResponse;
import com.moviecat.service.task.ReviewTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tasks", description = "Asynchronous task management")
@ApiResponses(value = {
    @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class TaskController {

    private final ReviewTaskService reviewTaskService;

    @GetMapping("/{taskId}")
    @Operation(summary = "Get async task status by task ID")
    public ResponseEntity<TaskStatusResponseDto> getTaskStatus(@PathVariable @Positive Long taskId) {
        return ResponseEntity.ok(reviewTaskService.getTaskStatus(taskId));
    }
}
