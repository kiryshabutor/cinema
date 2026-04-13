package com.moviecat.service.task;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.dto.TaskExecutionStatus;
import com.moviecat.dto.TaskStartResponseDto;
import com.moviecat.dto.TaskStatusResponseDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewTaskService {

    private final ReviewTaskRegistryService reviewTaskRegistryService;
    private final ReviewAsyncWorkerService reviewAsyncWorkerService;

    public TaskStartResponseDto startBulkCreateTask(
            Long movieId,
            List<ReviewCreateItemDto> reviewItems,
            boolean failOnPurpose,
            int startDelaySec,
            int itemDelaySec) {
        List<ReviewCreateItemDto> safeItems = Optional.ofNullable(reviewItems).orElseGet(List::of);
        UUID taskId = reviewTaskRegistryService.createTask(safeItems.size());
        reviewAsyncWorkerService.executeBulkTask(
                taskId,
                movieId,
                safeItems,
                failOnPurpose,
                startDelaySec,
                itemDelaySec);
        return new TaskStartResponseDto(taskId, TaskExecutionStatus.CREATED);
    }

    public TaskStatusResponseDto getTaskStatus(UUID taskId) {
        return reviewTaskRegistryService.getTaskStatus(taskId);
    }
}
