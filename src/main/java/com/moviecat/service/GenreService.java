package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Genre;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenreService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_DIRECTION = "asc";
    private static final String DESC_DIRECTION = "desc";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(DEFAULT_SORT_FIELD, "name");

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final MovieByIdCache movieByIdCache;
    private final MovieSearchCache movieSearchCache;

    public Page<Genre> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = PagingSortingUtils.normalizePage(page, DEFAULT_PAGE);
        int normalizedSize = PagingSortingUtils.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
        String normalizedSort = PagingSortingUtils.normalizeSort(
                sort, DEFAULT_SORT_FIELD, ALLOWED_SORT_FIELDS, false);
        String normalizedDirection = PagingSortingUtils.normalizeDirection(
                direction, DEFAULT_DIRECTION, DESC_DIRECTION);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
        return genreRepository.findAll(pageRequest);
    }

    public Genre getById(@NonNull Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    @Transactional
    public Genre create(Genre genre) {
        if (genreRepository.existsByName(genre.getName())) {
            throw new ResourceAlreadyExistsException("Genre with name '" + genre.getName() + "' already exists");
        }
        Genre savedGenre = genreRepository.save(genre);
        movieSearchCache.invalidate("GenreService.create");
        movieByIdCache.invalidate("GenreService.create");
        return savedGenre;
    }

    @Transactional
    public Genre update(@NonNull Long id, Genre genreDetails) {
        Genre genre = getById(id);

        if (genreRepository.existsByNameAndIdNot(genreDetails.getName(), id)) {
            throw new ResourceAlreadyExistsException("Genre with name '" + genreDetails.getName() + "' already exists");
        }

        genre.setName(genreDetails.getName());
        Genre updatedGenre = genreRepository.save(genre);
        movieSearchCache.invalidate("GenreService.update id=" + id);
        movieByIdCache.invalidate("GenreService.update id=" + id);
        return updatedGenre;
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre not found with id: " + id);
        }
        if (movieRepository.existsByGenresId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete genre with existing movies");
        }
        genreRepository.deleteById(id);
        movieSearchCache.invalidate("GenreService.delete id=" + id);
        movieByIdCache.invalidate("GenreService.delete id=" + id);
    }

}
