package com.moviecat.service;

import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.ReviewRepository;
import com.moviecat.repository.MovieRepository;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "authorAlias", "rating", "comment");

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDto> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);
        String normalizedDirection = normalizeDirection(direction);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                buildSort(normalizedSort, normalizedDirection));
        return reviewRepository.findAll(pageRequest).map(ReviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getByMovieId(Long movieId, int page, int size, String sort, String direction) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);
        String normalizedDirection = normalizeDirection(direction);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                buildSort(normalizedSort, normalizedDirection));
        return reviewRepository.findByMovieId(movieId, pageRequest).map(ReviewMapper::toDto);
    }

    @Transactional
    public ReviewDto create(ReviewDto dto) {
        if (dto.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required for a review");
        }

        Long movieId = Objects.requireNonNull(dto.getMovieId(), "Movie ID is required");
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        
        Review review = ReviewMapper.toEntity(dto);
        review.setId(null);
        review.setMovie(movie);
        
        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toDto(savedReview);
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String normalizeSort(String sort) {
        if (sort == null) {
            return DEFAULT_SORT_FIELD;
        }
        String trimmedSort = sort.trim();
        if (trimmedSort.isEmpty()) {
            return DEFAULT_SORT_FIELD;
        }
        if (!ALLOWED_SORT_FIELDS.contains(trimmedSort)) {
            return DEFAULT_SORT_FIELD;
        }
        return trimmedSort;
    }

    private String normalizeDirection(String direction) {
        if (direction == null) {
            return DEFAULT_DIRECTION;
        }
        String normalizedDirection = direction.trim().toLowerCase(Locale.ROOT);
        if (!DESC_DIRECTION.equals(normalizedDirection) && !DEFAULT_DIRECTION.equals(normalizedDirection)) {
            return DEFAULT_DIRECTION;
        }
        return normalizedDirection;
    }

    private Sort buildSort(String sort, String direction) {
        if (DESC_DIRECTION.equals(direction)) {
            return Sort.by(sort).descending();
        }
        return Sort.by(sort).ascending();
    }
}
