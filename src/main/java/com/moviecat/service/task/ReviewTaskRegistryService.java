package com.moviecat.service.task;

import com.moviecat.dto.TaskExecutionStatus;
import com.moviecat.dto.TaskStatusResponseDto;
import com.moviecat.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ReviewTaskRegistryService {

    private static final String TASK_NOT_FOUND_MSG = "Task not found with id: ";

    private final ConcurrentMap<Long, ReviewTaskState> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskSequence = new AtomicLong(0);

    public Long createTask(int totalCount) {
        long taskId = taskSequence.incrementAndGet();
        int normalizedTotalCount = Math.max(totalCount, 0);
        tasks.put(taskId, new ReviewTaskState(taskId, normalizedTotalCount));
        return taskId;
    }

    public TaskStatusResponseDto getTaskStatus(Long taskId) {
        return getTaskState(taskId).toResponseDto();
    }

    public void markRunning(Long taskId) {
        getTaskState(taskId).markRunning();
    }

    public void incrementProcessed(Long taskId) {
        getTaskState(taskId).incrementProcessed();
    }

    public void markCompleted(Long taskId) {
        getTaskState(taskId).markCompleted();
    }

    public void markFailed(Long taskId, String errorMessage) {
        getTaskState(taskId).markFailed(errorMessage);
    }

    private ReviewTaskState getTaskState(Long taskId) {
        ReviewTaskState taskState = tasks.get(taskId);
        if (taskState == null) {
            throw new ResourceNotFoundException(TASK_NOT_FOUND_MSG + taskId);
        }
        return taskState;
    }

    private static final class ReviewTaskState {
        private final Long taskId;
        private final int totalCount;
        private final LocalDateTime createdAt;
        private TaskExecutionStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private int processedCount;
        private String errorMessage;

        private ReviewTaskState(Long taskId, int totalCount) {
            this.taskId = taskId;
            this.totalCount = totalCount;
            this.createdAt = LocalDateTime.now();
            this.status = TaskExecutionStatus.CREATED;
            this.startedAt = null;
            this.finishedAt = null;
            this.processedCount = 0;
            this.errorMessage = null;
        }

        private synchronized void markRunning() {
            if (status != TaskExecutionStatus.CREATED) {
                return;
            }
            status = TaskExecutionStatus.RUNNING;
            startedAt = LocalDateTime.now();
        }

        private synchronized void incrementProcessed() {
            processedCount++;
        }

        private synchronized void markCompleted() {
            status = TaskExecutionStatus.COMPLETED;
            finishedAt = LocalDateTime.now();
        }

        private synchronized void markFailed(String message) {
            status = TaskExecutionStatus.FAILED;
            finishedAt = LocalDateTime.now();
            errorMessage = message;
        }

        private synchronized TaskStatusResponseDto toResponseDto() {
            return new TaskStatusResponseDto(
                    taskId,
                    status,
                    createdAt,
                    startedAt,
                    finishedAt,
                    totalCount,
                    processedCount,
                    errorMessage);
        }
    }
}
