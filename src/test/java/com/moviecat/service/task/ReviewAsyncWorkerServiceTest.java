package com.moviecat.service.task;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.exception.SimulatedFailureException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewAsyncWorkerServiceTest {

    @Mock
    private ReviewTaskRegistryService reviewTaskRegistryService;

    @Mock
    private ReviewBulkProcessingService reviewBulkProcessingService;

    private ReviewAsyncWorkerService reviewAsyncWorkerService;

    @BeforeEach
    void setUp() {
        reviewAsyncWorkerService = new ReviewAsyncWorkerService(reviewTaskRegistryService, reviewBulkProcessingService);
    }

    @Test
    void executeBulkTask_shouldMarkCompleted_whenProcessingSucceeds() {
        Long taskId = 1L;
        List<ReviewCreateItemDto> items = List.of(new ReviewCreateItemDto("alice", 9, "Great"));

        CompletableFuture<Void> future = reviewAsyncWorkerService.executeBulkTask(taskId, 7L, items, false, 0, 0);

        verify(reviewTaskRegistryService).markRunning(taskId);
        verify(reviewBulkProcessingService).createBulkTransactional(eq(7L), eq(items), eq(false), eq(0), any(Runnable.class));
        verify(reviewTaskRegistryService).markCompleted(taskId);
        verify(reviewTaskRegistryService, never()).markFailed(anyLong(), any(String.class));
        assertTrue(future.isDone());
    }

    @Test
    void executeBulkTask_shouldMarkFailed_whenProcessingThrowsException() {
        Long taskId = 2L;
        doThrow(new SimulatedFailureException("boom"))
                .when(reviewBulkProcessingService)
                .createBulkTransactional(anyLong(), anyList(), eq(true), eq(0), any(Runnable.class));

        reviewAsyncWorkerService.executeBulkTask(taskId, 7L, List.of(), true, 0, 0);

        verify(reviewTaskRegistryService).markRunning(taskId);
        verify(reviewTaskRegistryService).markFailed(taskId, "boom");
        verify(reviewTaskRegistryService, never()).markCompleted(taskId);
    }
}
