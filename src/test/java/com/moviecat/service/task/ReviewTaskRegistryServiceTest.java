package com.moviecat.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.moviecat.dto.TaskExecutionStatus;
import com.moviecat.dto.TaskStatusResponseDto;
import com.moviecat.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewTaskRegistryServiceTest {

    private ReviewTaskRegistryService reviewTaskRegistryService;

    @BeforeEach
    void setUp() {
        reviewTaskRegistryService = new ReviewTaskRegistryService();
    }

    @Test
    void createTask_shouldReturnCreatedStatus() {
        UUID taskId = reviewTaskRegistryService.createTask(3);

        TaskStatusResponseDto status = reviewTaskRegistryService.getTaskStatus(taskId);

        assertEquals(taskId, status.taskId());
        assertEquals(TaskExecutionStatus.CREATED, status.status());
        assertEquals(3, status.totalCount());
        assertEquals(0, status.processedCount());
        assertNotNull(status.createdAt());
        assertNull(status.startedAt());
        assertNull(status.finishedAt());
        assertNull(status.errorMessage());
    }

    @Test
    void markRunningAndCompleted_shouldUpdateStatusAndCounters() {
        UUID taskId = reviewTaskRegistryService.createTask(2);

        reviewTaskRegistryService.markRunning(taskId);
        reviewTaskRegistryService.incrementProcessed(taskId);
        reviewTaskRegistryService.incrementProcessed(taskId);
        reviewTaskRegistryService.markCompleted(taskId);

        TaskStatusResponseDto status = reviewTaskRegistryService.getTaskStatus(taskId);

        assertEquals(TaskExecutionStatus.COMPLETED, status.status());
        assertEquals(2, status.processedCount());
        assertNotNull(status.startedAt());
        assertNotNull(status.finishedAt());
        assertNull(status.errorMessage());
    }

    @Test
    void markFailed_shouldStoreFailureMessage() {
        UUID taskId = reviewTaskRegistryService.createTask(1);

        reviewTaskRegistryService.markRunning(taskId);
        reviewTaskRegistryService.markFailed(taskId, "boom");

        TaskStatusResponseDto status = reviewTaskRegistryService.getTaskStatus(taskId);

        assertEquals(TaskExecutionStatus.FAILED, status.status());
        assertEquals("boom", status.errorMessage());
        assertNotNull(status.finishedAt());
    }

    @Test
    void getTaskStatus_shouldThrow_whenTaskDoesNotExist() {
        UUID missingTaskId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> reviewTaskRegistryService.getTaskStatus(missingTaskId));
    }

    @Test
    void createTask_shouldNormalizeNegativeTotalCountToZero() {
        UUID taskId = reviewTaskRegistryService.createTask(-7);

        TaskStatusResponseDto status = reviewTaskRegistryService.getTaskStatus(taskId);

        assertEquals(0, status.totalCount());
    }

    @Test
    void markRunning_shouldBeIdempotent_whenCalledMultipleTimes() {
        UUID taskId = reviewTaskRegistryService.createTask(1);

        reviewTaskRegistryService.markRunning(taskId);
        TaskStatusResponseDto firstStatus = reviewTaskRegistryService.getTaskStatus(taskId);

        reviewTaskRegistryService.markRunning(taskId);
        TaskStatusResponseDto secondStatus = reviewTaskRegistryService.getTaskStatus(taskId);

        assertEquals(TaskExecutionStatus.RUNNING, secondStatus.status());
        assertEquals(firstStatus.startedAt(), secondStatus.startedAt());
    }
}
