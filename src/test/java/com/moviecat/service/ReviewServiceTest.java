package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.ReviewRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, movieRepository, movieByIdCache, movieSearchCache);
    }

    @Test
    void getAll_shouldNormalizePagingAndMapToDto() {
        Review review = review(1L, "critic", 9, "Great");
        Movie movie = new Movie();
        movie.setId(7L);
        review.setMovie(movie);
        PageRequest expectedRequest = PageRequest.of(0, 100, PagingSortingUtils.buildSort("id", "asc", "desc"));
        Page<Review> reviewPage = new PageImpl<>(nn(List.of(review)));

        when(reviewRepository.findAll(expectedRequest)).thenReturn(reviewPage);

        Page<ReviewDto> result = reviewService.getAll(-1, 500, "unsupported", "wrong");

        assertEquals(1, result.getTotalElements());
        assertEquals("critic", result.getContent().get(0).getAuthorAlias());
        assertEquals(7L, result.getContent().get(0).getMovieId());
        verify(reviewRepository).findAll(expectedRequest);
    }

    @Test
    void getByMovieId_shouldUseRequestedSortAndMapToDto() {
        Review review = review(2L, "bob", 7, "Ok");
        Movie movie = new Movie();
        movie.setId(9L);
        review.setMovie(movie);
        PageRequest expectedRequest = PageRequest.of(1, 20, PagingSortingUtils.buildSort("rating", "desc", "desc"));
        Page<Review> reviewPage = new PageImpl<>(nn(List.of(review)));

        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(reviewRepository.findByMovieId(9L, expectedRequest)).thenReturn(reviewPage);

        Page<ReviewDto> result = reviewService.getByMovieId(9L, 1, 20, "rating", "desc");

        assertEquals(1, result.getContent().size());
        assertEquals(7, result.getContent().get(0).getRating());
        verify(movieRepository).findById(9L);
        verify(reviewRepository).findByMovieId(9L, expectedRequest);
    }

    @Test
    void getByMovieId_shouldThrow_whenMovieNotFound() {
        when(movieRepository.findById(111L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getByMovieId(111L, 0, 10, "id", "asc"));

        verify(reviewRepository, never()).findByMovieId(any(), any(PageRequest.class));
    }

    @Test
    void create_shouldThrow_whenMovieIdIsNull() {
        ReviewDto dto = new ReviewDto(null, "critic", 8, "Good", null);

        assertThrows(IllegalArgumentException.class, () -> reviewService.create(dto));
    }

    @Test
    void create_shouldThrow_whenMovieNotFound() {
        ReviewDto dto = new ReviewDto(null, "critic", 8, "Good", 77L);
        when(movieRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.create(dto));
    }

    @Test
    void create_shouldResetIdSetMovieAndReturnSavedDto() {
        Movie movie = new Movie();
        movie.setId(7L);

        ReviewDto dto = new ReviewDto(99L, "critic", 8, "Good", 7L);
        Review savedReview = review(15L, "critic", 8, "Good");
        savedReview.setMovie(movie);

        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        when(reviewRepository.save(any(Review.class))).thenReturn(Objects.requireNonNull(savedReview));

        ReviewDto result = reviewService.create(dto);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review reviewToSave = Objects.requireNonNull(reviewCaptor.getValue());
        assertNull(reviewToSave.getId());
        assertSame(movie, reviewToSave.getMovie());
        assertEquals("critic", reviewToSave.getAuthorAlias());

        assertEquals(15L, result.getId());
        assertEquals(7L, result.getMovieId());
        assertEquals(8, result.getRating());
        verify(movieSearchCache).invalidate("ReviewService.create movieId=7");
        verify(movieByIdCache).evictAll(List.of(7L), "ReviewService.create movieId=7");
    }

    private Review review(Long id, String authorAlias, Integer rating, String comment) {
        Review review = new Review();
        review.setId(id);
        review.setAuthorAlias(authorAlias);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    private static <T> T nn(T value) {
        return Objects.requireNonNull(value);
    }
}
