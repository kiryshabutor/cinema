package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.repository.MovieRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class MovieViewWriteBehindServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private MovieViewWriteBehindService movieViewWriteBehindService;

    @BeforeEach
    void setUp() {
        movieViewWriteBehindService = new MovieViewWriteBehindService(movieRepository);
        ReflectionTestUtils.setField(movieViewWriteBehindService, "flushThreshold", 1000L);
    }

    @Test
    void incrementPendingAndGetCurrentViewCount_shouldReturnPersistedPlusPending() {
        when(movieRepository.findPersistedViewCountById(1L))
                .thenReturn(Optional.of(10L))
                .thenReturn(Optional.of(10L));

        long result = movieViewWriteBehindService.incrementPendingAndGetCurrentViewCount(1L);

        assertEquals(11L, result);
        verify(movieRepository, never()).incrementViewCountByDelta(anyLong(), anyLong());
    }

    @Test
    void incrementPendingAndGetCurrentViewCount_shouldThrow_whenMovieNotFound() {
        when(movieRepository.findPersistedViewCountById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> movieViewWriteBehindService.incrementPendingAndGetCurrentViewCount(404L));

        verify(movieRepository, never()).incrementViewCountByDelta(anyLong(), anyLong());
    }

    @Test
    void addPendingDeltaAndGetCurrentViewCount_shouldFlush_whenThresholdReached() {
        ReflectionTestUtils.setField(movieViewWriteBehindService, "flushThreshold", 3L);
        when(movieRepository.findPersistedViewCountById(2L))
                .thenReturn(Optional.of(20L))
                .thenReturn(Optional.of(23L));
        when(movieRepository.incrementViewCountByDelta(2L, 3L)).thenReturn(1);

        long result = movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(2L, 3L);

        assertEquals(23L, result);
        verify(movieRepository).incrementViewCountByDelta(2L, 3L);
    }

    @Test
    void addPendingDeltaAndGetCurrentViewCount_shouldIgnoreNonPositiveDelta() {
        when(movieRepository.findPersistedViewCountById(3L))
                .thenReturn(Optional.of(40L))
                .thenReturn(Optional.of(40L));

        long result = movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(3L, 0L);

        assertEquals(40L, result);
        verify(movieRepository, never()).incrementViewCountByDelta(anyLong(), anyLong());
    }

    @Test
    void addPendingDeltaAndGetCurrentViewCount_shouldNotAutoFlush_whenThresholdDisabled() {
        ReflectionTestUtils.setField(movieViewWriteBehindService, "flushThreshold", 0L);
        when(movieRepository.findPersistedViewCountById(31L))
                .thenReturn(Optional.of(20L))
                .thenReturn(Optional.of(20L));

        long result = movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(31L, 3L);

        assertEquals(23L, result);
        assertEquals(3L, movieViewWriteBehindService.getPendingDelta(31L));
        verify(movieRepository, never()).incrementViewCountByDelta(anyLong(), anyLong());
    }

    @Test
    void flushAllPendingDeltas_shouldFlushPendingForEachMovie() {
        when(movieRepository.findPersistedViewCountById(10L))
                .thenReturn(Optional.of(5L))
                .thenReturn(Optional.of(5L));
        when(movieRepository.findPersistedViewCountById(11L))
                .thenReturn(Optional.of(7L))
                .thenReturn(Optional.of(7L));
        when(movieRepository.incrementViewCountByDelta(10L, 2L)).thenReturn(1);
        when(movieRepository.incrementViewCountByDelta(11L, 3L)).thenReturn(1);

        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(10L, 2L);
        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(11L, 3L);
        movieViewWriteBehindService.flushAllPendingDeltas();

        verify(movieRepository).incrementViewCountByDelta(10L, 2L);
        verify(movieRepository).incrementViewCountByDelta(11L, 3L);
    }

    @Test
    void flushAllPendingDeltas_shouldSkipNullMovieIdEntry() {
        @SuppressWarnings("unchecked")
        ConcurrentMap<Long, AtomicLong> pendingMap = org.mockito.Mockito.mock(ConcurrentMap.class);
        Set<Long> keysWithNull = new HashSet<>();
        keysWithNull.add(null);
        when(pendingMap.keySet()).thenReturn(keysWithNull);
        ReflectionTestUtils.setField(movieViewWriteBehindService, "pendingViewDeltas", pendingMap);

        movieViewWriteBehindService.flushAllPendingDeltas();

        verifyNoInteractions(movieRepository);
    }

    @Test
    void flushMoviePendingDelta_shouldReturn_whenPendingCounterIsMissing() {
        movieViewWriteBehindService.flushMoviePendingDelta(99L);

        verifyNoInteractions(movieRepository);
    }

    @Test
    void flushMoviePendingDelta_shouldRemoveCounter_whenDeltaIsZero() {
        @SuppressWarnings("unchecked")
        ConcurrentMap<Long, AtomicLong> pendingMap =
                (ConcurrentMap<Long, AtomicLong>) ReflectionTestUtils.getField(movieViewWriteBehindService, "pendingViewDeltas");
        pendingMap.put(88L, new AtomicLong(0L));

        movieViewWriteBehindService.flushMoviePendingDelta(88L);

        assertFalse(pendingMap.containsKey(88L));
        verifyNoInteractions(movieRepository);
    }

    @Test
    void flushMoviePendingDelta_shouldNotFail_whenMovieDisappearedDuringFlush() {
        when(movieRepository.findPersistedViewCountById(41L))
                .thenReturn(Optional.of(10L))
                .thenReturn(Optional.of(10L));
        when(movieRepository.incrementViewCountByDelta(41L, 4L)).thenReturn(0);

        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(41L, 4L);

        assertDoesNotThrow(() -> movieViewWriteBehindService.flushMoviePendingDelta(41L));
        assertEquals(0L, movieViewWriteBehindService.getPendingDelta(41L));
        verify(movieRepository).incrementViewCountByDelta(41L, 4L);
    }

    @Test
    void flushMoviePendingDelta_shouldRestorePending_whenRepositoryFails() {
        when(movieRepository.findPersistedViewCountById(15L))
                .thenReturn(Optional.of(100L))
                .thenReturn(Optional.of(100L));
        when(movieRepository.incrementViewCountByDelta(15L, 5L))
                .thenThrow(new IllegalStateException("db is down"))
                .thenReturn(1);

        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(15L, 5L);

        assertThrows(IllegalStateException.class, () -> movieViewWriteBehindService.flushMoviePendingDelta(15L));
        movieViewWriteBehindService.flushMoviePendingDelta(15L);

        verify(movieRepository, times(2)).incrementViewCountByDelta(15L, 5L);
    }

    @Test
    void flushAllPendingDeltas_shouldContinue_whenSingleMovieFlushFails() {
        when(movieRepository.findPersistedViewCountById(20L))
                .thenReturn(Optional.of(1L))
                .thenReturn(Optional.of(1L));
        when(movieRepository.findPersistedViewCountById(21L))
                .thenReturn(Optional.of(1L))
                .thenReturn(Optional.of(1L));
        when(movieRepository.incrementViewCountByDelta(20L, 1L))
                .thenThrow(new IllegalStateException("db is down"));
        when(movieRepository.incrementViewCountByDelta(21L, 1L)).thenReturn(1);

        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(20L, 1L);
        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(21L, 1L);

        assertDoesNotThrow(() -> movieViewWriteBehindService.flushAllPendingDeltas());

        verify(movieRepository).incrementViewCountByDelta(20L, 1L);
        verify(movieRepository).incrementViewCountByDelta(21L, 1L);
    }

    @Test
    void flushPendingViewsOnShutdown_shouldFlushAccumulatedDelta() {
        when(movieRepository.findPersistedViewCountById(30L))
                .thenReturn(Optional.of(11L))
                .thenReturn(Optional.of(11L));
        when(movieRepository.incrementViewCountByDelta(30L, 4L)).thenReturn(1);

        movieViewWriteBehindService.addPendingDeltaAndGetCurrentViewCount(30L, 4L);
        movieViewWriteBehindService.flushPendingViewsOnShutdown();

        verify(movieRepository).incrementViewCountByDelta(30L, 4L);
    }
}
