package com.moviecat.repository;

import com.moviecat.model.Movie;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN m.director d " +
            "LEFT JOIN m.studio s " +
            "LEFT JOIN m.genres g " +
            "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:directorName IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :directorName, '%'))) " +
            "AND (:studioTitle IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :studioTitle, '%'))) " +
            "AND (:genreName IS NULL OR LOWER(g.name) = LOWER(:genreName))")
    Page<Movie> findAllByCriteria(@Param("title") String title,
                                  @Param("directorName") String directorName,
                                  @Param("studioTitle") String studioTitle,
                                  @Param("genreName") String genreName,
                                  Pageable pageable);

    @Query(value = "SELECT DISTINCT m.* FROM movies m " +
            "LEFT JOIN directors d ON m.director_id = d.id " +
            "LEFT JOIN studios s ON m.studio_id = s.id " +
            "LEFT JOIN movie_genre mg ON m.id = mg.movie_id " +
            "LEFT JOIN genres g ON mg.genre_id = g.id " +
            "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:directorName IS NULL OR LOWER(d.full_name) LIKE LOWER(CONCAT('%', :directorName, '%'))) " +
            "AND (:studioTitle IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :studioTitle, '%'))) " +
            "AND (:genreName IS NULL OR LOWER(g.name) = LOWER(:genreName))",
            countQuery = "SELECT count(DISTINCT m.id) FROM movies m " +
                    "LEFT JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN studios s ON m.studio_id = s.id " +
                    "LEFT JOIN movie_genre mg ON m.id = mg.movie_id " +
                    "LEFT JOIN genres g ON mg.genre_id = g.id " +
                    "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
                    "AND (:directorName IS NULL OR LOWER(d.full_name) LIKE LOWER(CONCAT('%', :directorName, '%'))) " +
                    "AND (:studioTitle IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :studioTitle, '%'))) " +
                    "AND (:genreName IS NULL OR LOWER(g.name) = LOWER(:genreName))",
            nativeQuery = true)
    Page<Movie> findAllByCriteriaNative(@Param("title") String title,
                                      @Param("directorName") String directorName,
                                      @Param("studioTitle") String studioTitle,
                                      @Param("genreName") String genreName,
                                      Pageable pageable);
}
