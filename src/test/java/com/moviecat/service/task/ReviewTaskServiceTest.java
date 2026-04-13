package com.moviecat.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.dto.TaskExecutionStatus;
import com.moviecat.dto.TaskStartResponseDto;
import com.moviecat.dto.TaskStatusResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewTaskServiceTest {

    @Mock
    private ReviewTaskRegistryService reviewTaskRegistryService;

    @Mock
    private ReviewAsyncWorkerService reviewAsyncWorkerService;

    private ReviewTaskService reviewTaskService;

    @BeforeEach
    void setUp() {
        reviewTaskService = new ReviewTaskService(reviewTaskRegistryService, reviewAsyncWorkerService);
    }

    @Test
    void startBulkCreateTask_shouldCreateTaskAndStartAsyncWorker() {
        UUID taskId = UUID.randomUUID();
        List<ReviewCreateItemDto> items = List.of(new ReviewCreateItemDto("alice", 9, "Great"));
        when(reviewTaskRegistryService.createTask(1)).thenReturn(taskId);

        TaskStartResponseDto response = reviewTaskService.startBulkCreateTask(7L, items, true, 2, 3);

        assertEquals(taskId, response.taskId());
        assertEquals(TaskExecutionStatus.CREATED, response.status());
        verify(reviewTaskRegistryService).createTask(1);
        verify(reviewAsyncWorkerService).executeBulkTask(taskId, 7L, items, true, 2, 3);
    }

    @Test
    void startBulkCreateTask_shouldTreatNullItemsAsEmptyList() {
        UUID taskId = UUID.randomUUID();
        when(reviewTaskRegistryService.createTask(0)).thenReturn(taskId);

        TaskStartResponseDto response = reviewTaskService.startBulkCreateTask(7L, null, false, 0, 0);

        assertEquals(taskId, response.taskId());
        assertEquals(TaskExecutionStatus.CREATED, response.status());
        verify(reviewTaskRegistryService).createTask(0);
        verify(reviewAsyncWorkerService).executeBulkTask(eq(taskId), eq(7L), anyList(), eq(false), eq(0), eq(0));
    }

    @Test
    void getTaskStatus_shouldDelegateToRegistry() {
        UUID taskId = UUID.randomUUID();
        TaskStatusResponseDto expected = new TaskStatusResponseDto(
                taskId,
                TaskExecutionStatus.RUNNING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                5,
                2,
                null);
        when(reviewTaskRegistryService.getTaskStatus(taskId)).thenReturn(expected);

        TaskStatusResponseDto actual = reviewTaskService.getTaskStatus(taskId);

        assertEquals(expected, actual);
        verify(reviewTaskRegistryService).getTaskStatus(taskId);
    }
}
