package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Genre;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    private GenreService genreService;

    @BeforeEach
    void setUp() {
        genreService = new GenreService(genreRepository, movieRepository, movieByIdCache, movieSearchCache);
    }

    @Test
    void getAll_shouldNormalizePagingAndSorting() {
        PageRequest expectedRequest = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(genreRepository.findAll(expectedRequest)).thenReturn(Page.empty());

        genreService.getAll(-1, 0, "unsupported", "wrong");

        verify(genreRepository).findAll(expectedRequest);
    }

    @Test
    void getById_shouldReturnGenre_whenExists() {
        Genre genre = genre(1L, "Sci-Fi");
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        Genre result = genreService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Sci-Fi", result.getName());
    }

    @Test
    void getById_shouldThrow_whenMissing() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.getById(1L));
    }

    @Test
    void create_shouldSaveGenre_whenUnique() {
        Genre input = genre(null, "Drama");
        Genre savedGenre = genre(5L, "Drama");
        when(genreRepository.existsByName("Drama")).thenReturn(false);
        when(genreRepository.save(input)).thenReturn(savedGenre);

        Genre result = genreService.create(input);

        assertEquals(5L, result.getId());
        assertEquals("Drama", result.getName());
    }

    @Test
    void create_shouldThrow_whenDuplicateName() {
        Genre input = genre(null, "Drama");
        when(genreRepository.existsByName("Drama")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> genreService.create(input));
        verify(genreRepository, never()).save(input);
    }

    @Test
    void update_shouldUpdateAndInvalidateCaches() {
        Long id = 7L;
        Genre existing = genre(id, "Old");
        Genre details = genre(null, "New");

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByNameAndIdNot("New", id)).thenReturn(false);
        when(movieRepository.findIdsByGenreId(id)).thenReturn(List.of(11L, 22L));
        when(genreRepository.save(existing)).thenReturn(existing);

        Genre result = genreService.update(id, details);

        assertEquals("New", result.getName());
        verify(movieSearchCache).invalidate("GenreService.update id=7");
        verify(movieByIdCache).evictAll(List.of(11L, 22L), "GenreService.update id=7");
    }

    @Test
    void update_shouldThrow_whenDuplicateName() {
        Long id = 7L;
        Genre existing = genre(id, "Old");
        Genre details = genre(null, "New");

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByNameAndIdNot("New", id)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> genreService.update(id, details));

        verify(movieSearchCache, never()).invalidate(any());
        verify(movieByIdCache, never()).evictAll(any(), any());
    }

    @Test
    void delete_shouldThrow_whenGenreMissing() {
        when(genreRepository.existsById(3L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> genreService.delete(3L));
    }

    @Test
    void delete_shouldThrow_whenGenreHasMovies() {
        when(genreRepository.existsById(3L)).thenReturn(true);
        when(movieRepository.existsByGenresId(3L)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> genreService.delete(3L));

        verify(genreRepository, never()).deleteById(3L);
    }

    @Test
    void delete_shouldDelete_whenNoRelatedMovies() {
        when(genreRepository.existsById(3L)).thenReturn(true);
        when(movieRepository.existsByGenresId(3L)).thenReturn(false);

        genreService.delete(3L);

        verify(genreRepository).deleteById(3L);
    }

    private Genre genre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }
}
