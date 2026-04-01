package com.moviecat.service;

import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.repository.MovieRepository;
import jakarta.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieViewWriteBehindService {

    private static final String MOVIE_NOT_FOUND_MSG = "Movie not found with id: ";
    private static final String MOVIE_ID_REQUIRED_MSG = "movieId";

    private final MovieRepository movieRepository;
    private final ConcurrentMap<Long, AtomicLong> pendingViewDeltas = new ConcurrentHashMap<>();

    @Value("${movie.views.write-behind.flush-threshold:1000}")
    private long flushThreshold;

    public long incrementPendingAndGetCurrentViewCount(@NonNull Long movieId) {
        Long safeMovieId = requireMovieId(movieId);
        ensureMovieExists(safeMovieId);
        addPendingDeltaInternal(safeMovieId, 1L);
        return getCurrentViewCount(safeMovieId);
    }

    public long addPendingDeltaAndGetCurrentViewCount(@NonNull Long movieId, long delta) {
        Long safeMovieId = requireMovieId(movieId);
        ensureMovieExists(safeMovieId);
        if (delta <= 0L) {
            return getCurrentViewCount(safeMovieId);
        }
        addPendingDeltaInternal(safeMovieId, delta);
        return getCurrentViewCount(safeMovieId);
    }

    public long getCurrentViewCount(@NonNull Long movieId) {
        Long safeMovieId = requireMovieId(movieId);
        long persistedViewCount = getPersistedViewCountOrThrow(safeMovieId);
        long pendingDelta = getPendingDelta(safeMovieId);
        return persistedViewCount + pendingDelta;
    }

    public long getPendingDelta(@NonNull Long movieId) {
        Long safeMovieId = requireMovieId(movieId);
        AtomicLong pendingCounter = pendingViewDeltas.get(safeMovieId);
        return pendingCounter == null ? 0L : pendingCounter.get();
    }

    public void ensureMovieExists(@NonNull Long movieId) {
        Long safeMovieId = requireMovieId(movieId);
        getPersistedViewCountOrThrow(safeMovieId);
    }

    @Scheduled(
            fixedDelayString = "${movie.views.write-behind.flush-interval-sec:30}",
            timeUnit = TimeUnit.SECONDS)
    public void flushAllPendingDeltas() {
        for (Long movieId : pendingViewDeltas.keySet()) {
            if (movieId == null) {
                continue;
            }
            try {
                flushMoviePendingDelta(movieId);
            } catch (RuntimeException exception) {
                log.warn("Failed to flush pending views for movieId={}", movieId, exception);
            }
        }
    }

    @PreDestroy
    public void flushPendingViewsOnShutdown() {
        flushAllPendingDeltas();
    }

    void flushMoviePendingDelta(@NonNull Long movieId) {
        Long safeMovieId = requireMovieId(movieId);
        AtomicLong pendingCounter = pendingViewDeltas.get(safeMovieId);
        if (pendingCounter == null) {
            return;
        }

        long deltaToFlush = pendingCounter.getAndSet(0L);
        if (deltaToFlush <= 0L) {
            removeCounterIfZero(safeMovieId, pendingCounter);
            return;
        }

        try {
            int updatedRows = movieRepository.incrementViewCountByDelta(safeMovieId, deltaToFlush);
            if (updatedRows == 0) {
                log.warn("Movie is missing during pending view flush: movieId={}, delta={}", safeMovieId, deltaToFlush);
            }
        } catch (RuntimeException exception) {
            pendingCounter.addAndGet(deltaToFlush);
            throw exception;
        } finally {
            removeCounterIfZero(safeMovieId, pendingCounter);
        }
    }

    private long getPersistedViewCountOrThrow(Long movieId) {
        return movieRepository.findPersistedViewCountById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + movieId));
    }

    private void addPendingDeltaInternal(@NonNull Long movieId, long delta) {
        Long safeMovieId = requireMovieId(movieId);
        AtomicLong pendingCounter = pendingViewDeltas.computeIfAbsent(safeMovieId, id -> new AtomicLong(0L));
        long pendingAfterUpdate = pendingCounter.addAndGet(delta);
        if (flushThreshold > 0 && pendingAfterUpdate >= flushThreshold) {
            flushMoviePendingDelta(safeMovieId);
        }
    }

    private void removeCounterIfZero(Long movieId, AtomicLong pendingCounter) {
        if (pendingCounter.get() == 0L) {
            pendingViewDeltas.remove(movieId, pendingCounter);
        }
    }

    private @NonNull Long requireMovieId(Long movieId) {
        return Objects.requireNonNull(movieId, MOVIE_ID_REQUIRED_MSG);
    }
}
