package com.moviecat.service.task;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.SimulatedFailureException;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewBulkProcessingService {

    private static final String MOVIE_ID_REQUIRED_MSG = "Movie ID is required";
    private static final String SIMULATED_FAILURE_MSG = "Simulated failure during review processing";
    private static final String INTERRUPTED_MSG = "Bulk processing was interrupted";

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public void createBulkTransactional(
            Long movieId,
            List<ReviewCreateItemDto> reviewItems,
            boolean failOnPurpose,
            int itemDelaySec,
            Runnable onReviewProcessed) {
        Movie movie = findMovieById(movieId);
        List<ReviewCreateItemDto> safeItems = Optional.ofNullable(reviewItems).orElseGet(List::of);
        Runnable safeProcessedCallback = onReviewProcessed != null ? onReviewProcessed : () -> {
        };

        for (int i = 0; i < safeItems.size(); i++) {
            sleepSeconds(itemDelaySec);

            if (failOnPurpose && i == safeItems.size() - 1) {
                throw new SimulatedFailureException(SIMULATED_FAILURE_MSG);
            }

            Review review = ReviewMapper.toEntity(safeItems.get(i));
            review.setId(null);
            review.setMovie(movie);
            reviewRepository.save(review);
            safeProcessedCallback.run();
        }
    }

    private Movie findMovieById(Long movieId) {
        Long safeMovieId = Objects.requireNonNull(movieId, MOVIE_ID_REQUIRED_MSG);
        return movieRepository.findById(safeMovieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + safeMovieId));
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
