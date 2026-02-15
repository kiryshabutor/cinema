package com.moviecat.repository;

import com.moviecat.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    java.util.List<Review> findByMovieId(Long movieId);
}
