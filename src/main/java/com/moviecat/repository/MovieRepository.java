package com.moviecat.repository;

import com.moviecat.model.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing movie data.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);

    boolean existsByDirectorId(Long directorId);

    boolean existsByStudioId(Long studioId);

    boolean existsByGenresId(Long genreId);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = {"genres", "director", "studio"})
    @Query("SELECT m FROM Movie m")
    List<Movie> findAllWithDetails();
}
