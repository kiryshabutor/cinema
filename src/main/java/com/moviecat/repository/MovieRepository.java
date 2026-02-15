package com.moviecat.repository;

import com.moviecat.model.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);

    boolean existsByDirectorId(Long directorId);

    boolean existsByStudioId(Long studioId);

    boolean existsByGenresId(Long genreId);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"genres", "director", "studio"})
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Movie m")
    List<Movie> findAllWithDetails();
}
