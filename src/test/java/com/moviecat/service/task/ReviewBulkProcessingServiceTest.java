package com.moviecat.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private ReviewBulkProcessingService reviewBulkProcessingService;

    @BeforeEach
    void setUp() {
        reviewBulkProcessingService = new ReviewBulkProcessingService(reviewRepository, movieRepository);
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
    }

    @Test
    void createBulkTransactional_shouldThrow_whenMovieNotFound() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reviewBulkProcessingService.createBulkTransactional(404L, List.of(), false, 0, () -> {
                }));

        verifyNoInteractions(reviewRepository);
    }
}
