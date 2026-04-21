package com.moviecat.repository;

import com.moviecat.model.Review;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByMovieId(Long movieId, Pageable pageable);

    @Query(
            """
            SELECT
                r.movie.id AS movieId,
                AVG(r.rating) AS averageRating,
                COUNT(r.id) AS reviewCount
            FROM Review r
            WHERE r.movie.id IN :movieIds
            GROUP BY r.movie.id
            """)
    List<MovieRatingSummaryProjection> summarizeRatingsByMovieIds(@Param("movieIds") Collection<Long> movieIds);

    interface MovieRatingSummaryProjection {
        Long getMovieId();

        Double getAverageRating();

        Long getReviewCount();
    }
}
