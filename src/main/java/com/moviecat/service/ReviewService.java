package com.moviecat.service;

import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public List<ReviewDto> getAll() {
        return reviewRepository.findAll().stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getByMovieId(Long movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
                .map(ReviewMapper::toDto)
                .toList();
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
}
