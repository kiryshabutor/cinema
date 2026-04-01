package com.moviecat.repository;

import com.moviecat.model.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);

    boolean existsByDirectorId(Long directorId);

    boolean existsByStudioId(Long studioId);

    boolean existsByGenresId(Long genreId);

    @Query("SELECT m.id FROM Movie m WHERE m.director.id = :directorId")
    List<Long> findIdsByDirectorId(@Param("directorId") Long directorId);

    @Query("SELECT m.id FROM Movie m WHERE m.studio.id = :studioId")
    List<Long> findIdsByStudioId(@Param("studioId") Long studioId);

    @Query(
            """
            SELECT DISTINCT m.id
            FROM Movie m
            JOIN m.genres g
            WHERE g.id = :genreId
            """)
    List<Long> findIdsByGenreId(@Param("genreId") Long genreId);

    @Query(
            value = """
            SELECT
                m.id AS id,
                m.title AS title,
                m.year AS year,
                m.duration AS duration,
                m.viewCount AS viewCount,
                m.posterUrl AS posterUrl,
                d.id AS directorId,
                d.lastName AS directorLastName,
                d.firstName AS directorFirstName,
                d.middleName AS directorMiddleName,
                s.id AS studioId,
                s.title AS studioTitle
            FROM Movie m
            LEFT JOIN m.director d
            LEFT JOIN m.studio s
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.lastName) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR EXISTS (
                     SELECT 1
                     FROM m.genres g
                     WHERE LOWER(g.name) LIKE CONCAT('%', :genreName, '%')
                 ))
            """,
            countQuery = """
            SELECT COUNT(m.id)
            FROM Movie m
            LEFT JOIN m.director d
            LEFT JOIN m.studio s
            WHERE (:title = '' OR LOWER(m.title) LIKE CONCAT('%', :title, '%'))
              AND (:directorLastName = '' OR LOWER(d.lastName) LIKE CONCAT('%', :directorLastName, '%'))
              AND (:studioTitle = '' OR LOWER(s.title) LIKE CONCAT('%', :studioTitle, '%'))
              AND (:genreName = '' OR EXISTS (
                     SELECT 1
                     FROM m.genres g
                     WHERE LOWER(g.name) LIKE CONCAT('%', :genreName, '%')
                 ))
            """)
    Page<MovieSearchRowProjection> searchAdvancedJpql(
            @Param("title") String title,
            @Param("directorLastName") String directorLastName,
            @Param("genreName") String genreName,
            @Param("studioTitle") String studioTitle,
            Pageable pageable);

    @Query(
            value = """
            SELECT
                m.id AS "id",
                m.title AS "title",
                m.release_year AS "year",
                m.duration AS "duration",
                m.view_count AS "viewCount",
                m.poster_url AS "posterUrl",
                d.id AS "directorId",
                d.last_name AS "directorLastName",
                d.first_name AS "directorFirstName",
                d.middle_name AS "directorMiddleName",
                s.id AS "studioId",
                s.title AS "studioTitle"
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
    Page<MovieSearchRowProjection> searchAdvancedNative(
            @Param("title") String title,
            @Param("directorLastName") String directorLastName,
            @Param("genreName") String genreName,
            @Param("studioTitle") String studioTitle,
            @Param("sortField") String sortField,
            @Param("sortDirection") String sortDirection,
            Pageable pageable);

    @Query(
            """
            SELECT DISTINCT m
            FROM Movie m
            LEFT JOIN FETCH m.director
            LEFT JOIN FETCH m.studio
            LEFT JOIN FETCH m.genres
            """)
    List<Movie> findAllWithDetails();

    @Query(
            """
            SELECT DISTINCT m
            FROM Movie m
            LEFT JOIN FETCH m.director
            LEFT JOIN FETCH m.studio
            LEFT JOIN FETCH m.genres
            WHERE m.id = :id
            """)
    Optional<Movie> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COALESCE(m.viewCount, 0) FROM Movie m WHERE m.id = :movieId")
    Optional<Long> findPersistedViewCountById(@Param("movieId") Long movieId);

    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE movies
            SET view_count = COALESCE(view_count, 0) + :delta
            WHERE id = :movieId
            """,
            nativeQuery = true)
    int incrementViewCountByDelta(@Param("movieId") Long movieId, @Param("delta") long delta);

    interface MovieSearchRowProjection {
        Long getId();

        String getTitle();

        Integer getYear();

        Integer getDuration();

        Long getViewCount();

        String getPosterUrl();

        Long getDirectorId();

        String getDirectorLastName();

        String getDirectorFirstName();

        String getDirectorMiddleName();

        Long getStudioId();

        String getStudioTitle();
    }
}
