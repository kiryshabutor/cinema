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

    @Query(
            value = """
            SELECT DISTINCT m
            FROM Movie m
            LEFT JOIN m.director d
            LEFT JOIN m.studio s
            LEFT JOIN m.genres g
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.lastName) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR LOWER(g.name) LIKE CONCAT('%', :genreName, '%'))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT m.id)
            FROM Movie m
            LEFT JOIN m.director d
            LEFT JOIN m.studio s
            LEFT JOIN m.genres g
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.lastName) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR LOWER(g.name) LIKE CONCAT('%', :genreName, '%'))
            """)
    Page<Movie> searchAdvancedJpql(
            @Param("title") String title,
            @Param("directorLastName") String directorLastName,
            @Param("genreName") String genreName,
            @Param("studioTitle") String studioTitle,
            Pageable pageable);

    @Query(
            value = """
            SELECT m.*
            FROM movies m
            LEFT JOIN directors d ON d.id = m.director_id
            LEFT JOIN studios s ON s.id = m.studio_id
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.last_name) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR EXISTS (
                     SELECT 1
                     FROM movie_genre mg
                     JOIN genres g ON g.id = mg.genre_id
                     WHERE mg.movie_id = m.id
                       AND LOWER(g.name) LIKE CONCAT('%', :genreName, '%')
                 ))
            ORDER BY
              CASE WHEN :sortField = 'title' AND :sortDirection = 'asc' THEN m.title END ASC,
              CASE WHEN :sortField = 'title' AND :sortDirection = 'desc' THEN m.title END DESC,
              CASE WHEN :sortField = 'year' AND :sortDirection = 'asc' THEN m.release_year END ASC,
              CASE WHEN :sortField = 'year' AND :sortDirection = 'desc' THEN m.release_year END DESC,
              CASE WHEN :sortField = 'viewCount' AND :sortDirection = 'asc' THEN m.view_count END ASC,
              CASE WHEN :sortField = 'viewCount' AND :sortDirection = 'desc' THEN m.view_count END DESC,
              CASE WHEN :sortField = 'id' AND :sortDirection = 'asc' THEN m.id END ASC,
              CASE WHEN :sortField = 'id' AND :sortDirection = 'desc' THEN m.id END DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM movies m
            LEFT JOIN directors d ON d.id = m.director_id
            LEFT JOIN studios s ON s.id = m.studio_id
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.last_name) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR EXISTS (
                     SELECT 1
                     FROM movie_genre mg
                     JOIN genres g ON g.id = mg.genre_id
                     WHERE mg.movie_id = m.id
                       AND LOWER(g.name) LIKE CONCAT('%', :genreName, '%')
                 ))
            """,
            nativeQuery = true)
    Page<Movie> searchAdvancedNative(
            @Param("title") String title,
            @Param("directorLastName") String directorLastName,
            @Param("genreName") String genreName,
            @Param("studioTitle") String studioTitle,
            @Param("sortField") String sortField,
            @Param("sortDirection") String sortDirection,
            Pageable pageable);

    @EntityGraph(attributePaths = {"genres", "director", "studio"})
    @Query("SELECT m FROM Movie m")
    List<Movie> findAllWithDetails();
}
