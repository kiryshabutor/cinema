package com.moviecat.service;

import com.moviecat.dto.ReviewCreateItemDto;
import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.SimulatedFailureException;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_DIRECTION = "asc";
    private static final String DESC_DIRECTION = "desc";
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(DEFAULT_SORT_FIELD, "authorAlias", "rating", "comment");

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDto> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = PagingSortingUtils.normalizePage(page, DEFAULT_PAGE);
        int normalizedSize = PagingSortingUtils.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
        String normalizedSort = PagingSortingUtils.normalizeSort(
                sort, DEFAULT_SORT_FIELD, ALLOWED_SORT_FIELDS, false);
        String normalizedDirection = PagingSortingUtils.normalizeDirection(
                direction, DEFAULT_DIRECTION, DESC_DIRECTION);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
        return reviewRepository.findAll(pageRequest).map(ReviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getByMovieId(Long movieId, int page, int size, String sort, String direction) {
        int normalizedPage = PagingSortingUtils.normalizePage(page, DEFAULT_PAGE);
        int normalizedSize = PagingSortingUtils.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
        String normalizedSort = PagingSortingUtils.normalizeSort(
                sort, DEFAULT_SORT_FIELD, ALLOWED_SORT_FIELDS, false);
        String normalizedDirection = PagingSortingUtils.normalizeDirection(
                direction, DEFAULT_DIRECTION, DESC_DIRECTION);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
        return reviewRepository.findByMovieId(movieId, pageRequest).map(ReviewMapper::toDto);
    }

    @Transactional
    public ReviewDto create(ReviewDto dto) {
        if (dto.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required for a review");
        }

        Long movieId = Objects.requireNonNull(dto.getMovieId(), "Movie ID is required");
        Movie movie = findMovieById(movieId);

        Review review = ReviewMapper.toEntity(dto);
        review.setId(null);
        review.setMovie(movie);

        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toDto(savedReview);
    }

    @Transactional
    public List<ReviewDto> createBulkTransactional(Long movieId,
                                                   List<ReviewCreateItemDto> reviewItems,
                                                   boolean failOnPurpose) {
        return createBulkInternal(movieId, reviewItems, failOnPurpose);
    }

    public List<ReviewDto> createBulkNonTransactional(Long movieId,
                                                      List<ReviewCreateItemDto> reviewItems,
                                                      boolean failOnPurpose) {
        return createBulkInternal(movieId, reviewItems, failOnPurpose);
    }

    private List<ReviewDto> createBulkInternal(Long movieId,
                                               List<ReviewCreateItemDto> reviewItems,
                                               boolean failOnPurpose) {
        Movie movie = findMovieById(movieId);
        List<ReviewCreateItemDto> safeItems = Optional.ofNullable(reviewItems).orElseGet(List::of);

        List<Review> preparedReviews = safeItems.stream()
                .map(ReviewMapper::toEntity)
                .toList();

        List<ReviewDto> createdReviews = new ArrayList<>(preparedReviews.size());
        for (int i = 0; i < preparedReviews.size(); i++) {
            if (failOnPurpose && i == preparedReviews.size() - 1) {
                throw new SimulatedFailureException("Simulated failure during review processing");
            }

            Review review = preparedReviews.get(i);
            review.setId(null);
            review.setMovie(movie);
            Review savedReview = reviewRepository.save(review);
            createdReviews.add(ReviewMapper.toDto(savedReview));
        }
        return createdReviews;
    }

    private Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
    }
}
