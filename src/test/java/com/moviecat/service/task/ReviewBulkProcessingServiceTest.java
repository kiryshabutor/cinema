package com.moviecat.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.SimulatedFailureException;
import com.moviecat.model.Movie;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewBulkProcessingServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    private ReviewBulkProcessingService reviewBulkProcessingService;

    @BeforeEach
    void setUp() {
        reviewBulkProcessingService =
                new ReviewBulkProcessingService(reviewRepository, movieRepository, movieByIdCache, movieSearchCache);
    }

    @Test
    void createBulkTransactional_shouldSaveAllReviewsAndInvokeCallback() {
        Movie movie = new Movie();
        movie.setId(7L);
        List<ReviewCreateItemDto> items = List.of(
                new ReviewCreateItemDto("alice", 9, "Great"),
                new ReviewCreateItemDto("bob", 8, "Good"));
        AtomicInteger processedCounter = new AtomicInteger(0);

        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));

        reviewBulkProcessingService.createBulkTransactional(7L, items, false, 0, processedCounter::incrementAndGet);

        long saveCalls = mockingDetails(reviewRepository).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .count();
        assertEquals(2, saveCalls);
        assertEquals(2, processedCounter.get());
        org.mockito.Mockito.verify(movieSearchCache)
                .invalidate("ReviewBulkProcessingService.createBulkTransactional movieId=7");
        org.mockito.Mockito.verify(movieByIdCache).evictAll(
                java.util.Set.of(7L),
                "ReviewBulkProcessingService.createBulkTransactional movieId=7");
    }

    @Test
    void createBulkTransactional_shouldThrowAndStopOnLastReview_whenFailIsEnabled() {
        Movie movie = new Movie();
        movie.setId(7L);
        List<ReviewCreateItemDto> items = List.of(
                new ReviewCreateItemDto("alice", 9, "Great"),
                new ReviewCreateItemDto("bob", 8, "Good"));
        AtomicInteger processedCounter = new AtomicInteger(0);

        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));

        assertThrows(
                SimulatedFailureException.class,
                () -> reviewBulkProcessingService.createBulkTransactional(
                        7L,
                        items,
                        true,
                        0,
                        processedCounter::incrementAndGet));

        long saveCalls = mockingDetails(reviewRepository).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .count();
        assertEquals(1, saveCalls);
        assertEquals(1, processedCounter.get());
        verifyNoInteractions(movieSearchCache, movieByIdCache);
    }

    @Test
    void createBulkTransactional_shouldThrow_whenMovieNotFound() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());
        List<ReviewCreateItemDto> emptyItems = List.of();
        Runnable noOp = () -> {
        };

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewBulkProcessingService.createBulkTransactional(404L, emptyItems, false, 0, noOp));

        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createBulkTransactional_shouldHandleNullItemsAndCallback() {
        Movie movie = new Movie();
        movie.setId(7L);
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));

        assertDoesNotThrow(() -> reviewBulkProcessingService.createBulkTransactional(7L, null, false, 0, null));

        verifyNoInteractions(reviewRepository, movieSearchCache, movieByIdCache);
    }

    @Test
    void createBulkTransactional_shouldSleepAndUseNoOpCallback_whenDelayPositive() {
        Movie movie = new Movie();
        movie.setId(7L);
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        List<ReviewCreateItemDto> items = List.of(new ReviewCreateItemDto("alice", 9, "Great"));

        assertDoesNotThrow(() -> reviewBulkProcessingService.createBulkTransactional(7L, items, false, 1, null));

        long saveCalls = mockingDetails(reviewRepository).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .count();
        assertEquals(1, saveCalls);
        org.mockito.Mockito.verify(movieSearchCache)
                .invalidate("ReviewBulkProcessingService.createBulkTransactional movieId=7");
        org.mockito.Mockito.verify(movieByIdCache).evictAll(
                java.util.Set.of(7L),
                "ReviewBulkProcessingService.createBulkTransactional movieId=7");
    }

    @Test
    void createBulkTransactional_shouldThrow_whenMovieIdIsNull() {
        List<ReviewCreateItemDto> emptyItems = List.of();
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reviewBulkProcessingService.createBulkTransactional(null, emptyItems, false, 0, null));

        assertEquals("Movie ID is required", exception.getMessage());
        verifyNoInteractions(movieRepository, reviewRepository, movieSearchCache, movieByIdCache);
    }

    @Test
    void createBulkTransactional_shouldThrow_whenInterruptedDuringItemDelay() {
        Movie movie = new Movie();
        movie.setId(7L);
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        List<ReviewCreateItemDto> items = List.of(new ReviewCreateItemDto("alice", 9, "Great"));

        Thread.currentThread().interrupt();
        try {
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> reviewBulkProcessingService.createBulkTransactional(7L, items, false, 1, null));

            assertEquals("Bulk processing was interrupted", exception.getMessage());
            verifyNoInteractions(reviewRepository, movieSearchCache, movieByIdCache);
        } finally {
            Thread.interrupted();
        }
    }
}
