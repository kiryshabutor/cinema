package com.moviecat.mapper;

import com.moviecat.dto.ReviewDto;
import com.moviecat.model.Review;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setAuthorAlias(review.getAuthorAlias());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        if (review.getMovie() != null) {
            dto.setMovieId(review.getMovie().getId());
        }
        return dto;
    }

    public static Review toEntity(ReviewDto dto) {
        Review review = new Review();
        review.setId(dto.getId());
        review.setAuthorAlias(dto.getAuthorAlias());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }
}
