package com.moviecat.service.task;

import com.moviecat.dto.ReviewCreateItemDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewAsyncWorkerService {

    private static final String INTERRUPTED_MSG = "Task was interrupted";
    private static final String UNKNOWN_ERROR_MSG = "Task failed due to unexpected error";

    private final ReviewTaskRegistryService reviewTaskRegistryService;
    private final ReviewBulkProcessingService reviewBulkProcessingService;

    @Async("reviewTaskExecutor")
    public CompletableFuture<Void> executeBulkTask(
            Long taskId,
            Long movieId,
            List<ReviewCreateItemDto> reviewItems,
            boolean failOnPurpose,
            int startDelaySec,
            int itemDelaySec) {
        try {
            sleepSeconds(startDelaySec);
            reviewTaskRegistryService.markRunning(taskId);
            reviewBulkProcessingService.createBulkTransactional(
                    movieId,
                    reviewItems,
                    failOnPurpose,
                    itemDelaySec,
                    () -> reviewTaskRegistryService.incrementProcessed(taskId));
            reviewTaskRegistryService.markCompleted(taskId);
        } catch (Exception exception) {
            reviewTaskRegistryService.markFailed(taskId, resolveErrorMessage(exception));
        }

        return CompletableFuture.completedFuture(null);
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return UNKNOWN_ERROR_MSG;
        }
        return message;
    }

    private void sleepSeconds(int delaySec) {
        if (delaySec <= 0) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(delaySec);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(INTERRUPTED_MSG, exception);
        }
    }
}
