package com.moviecat.service;

import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import java.util.Objects;
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
    private static final String MOVIE_ID_REQUIRED_MSG = "Movie ID is required";
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
        Long safeMovieId = Objects.requireNonNull(movieId, MOVIE_ID_REQUIRED_MSG);
        findMovieById(safeMovieId);

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
        return reviewRepository.findByMovieId(safeMovieId, pageRequest).map(ReviewMapper::toDto);
    }

    @Transactional
    public ReviewDto create(ReviewDto dto) {
        if (dto.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required for a review");
        }

        Long movieId = Objects.requireNonNull(dto.getMovieId(), MOVIE_ID_REQUIRED_MSG);
        Movie movie = findMovieById(movieId);

        Review review = ReviewMapper.toEntity(dto);
        review.setId(null);
        review.setMovie(movie);

        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toDto(savedReview);
    }

    private Movie findMovieById(Long movieId) {
        Long safeMovieId = Objects.requireNonNull(movieId, MOVIE_ID_REQUIRED_MSG);
        return movieRepository.findById(safeMovieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + safeMovieId));
    }
}
