package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Director;
import com.moviecat.repository.DirectorRepository;
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
class DirectorServiceTest {

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    private DirectorService directorService;

    @BeforeEach
    void setUp() {
        directorService = new DirectorService(directorRepository, movieRepository, movieByIdCache, movieSearchCache);
    }

    @Test
    void getAll_shouldNormalizePagingAndSorting() {
        PageRequest expectedRequest = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(directorRepository.findAll(expectedRequest)).thenReturn(Page.empty());

        directorService.getAll(-5, 0, "unknown", "wrong");

        verify(directorRepository).findAll(expectedRequest);
    }

    @Test
    void getById_shouldReturnDirector_whenExists() {
        Director director = director(1L, "Nolan", "Christopher", null);
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));

        Director result = directorService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Nolan", result.getLastName());
    }

    @Test
    void getById_shouldThrow_whenMissing() {
        when(directorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> directorService.getById(1L));
    }

    @Test
    void create_shouldTrimNamesAndSave() {
        Director input = director(null, " Nolan ", " Christopher ", "   ");
        Director savedDirector = director(10L, "Nolan", "Christopher", null);
        when(directorRepository.existsByLastNameAndFirstNameAndMiddleName("Nolan", "Christopher", null))
                .thenReturn(false);
        when(directorRepository.save(input)).thenReturn(savedDirector);

        Director result = directorService.create(input);

        assertEquals(10L, result.getId());
        assertEquals("Nolan", result.getLastName());
        assertEquals("Christopher", result.getFirstName());
        assertNull(result.getMiddleName());
    }

    @Test
    void create_shouldThrow_whenDuplicateName() {
        Director input = director(null, "Nolan", "Christopher", null);
        when(directorRepository.existsByLastNameAndFirstNameAndMiddleName("Nolan", "Christopher", null))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> directorService.create(input));
        verify(directorRepository, never()).save(input);
    }

    @Test
    void update_shouldUpdateDirectorAndInvalidateRelatedCaches() {
        Long id = 2L;
        Director existing = director(id, "Old", "Name", "M");
        Director details = director(null, " Nolan ", " Christopher ", " ");

        when(directorRepository.findById(id)).thenReturn(Optional.of(existing));
        when(directorRepository.existsByLastNameAndFirstNameAndMiddleNameAndIdNot("Nolan", "Christopher", null, id))
                .thenReturn(false);
        when(movieRepository.findIdsByDirectorId(id)).thenReturn(List.of(100L, 101L));
        when(directorRepository.save(existing)).thenReturn(existing);

        Director result = directorService.update(id, details);

        assertEquals("Nolan", result.getLastName());
        assertEquals("Christopher", result.getFirstName());
        assertNull(result.getMiddleName());
        verify(movieSearchCache).invalidate("DirectorService.update id=2");
        verify(movieByIdCache).evictAll(List.of(100L, 101L), "DirectorService.update id=2");
    }

    @Test
    void update_shouldThrow_whenDuplicateName() {
        Long id = 2L;
        Director existing = director(id, "Old", "Name", null);
        Director details = director(null, "Nolan", "Christopher", null);

        when(directorRepository.findById(id)).thenReturn(Optional.of(existing));
        when(directorRepository.existsByLastNameAndFirstNameAndMiddleNameAndIdNot("Nolan", "Christopher", null, id))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> directorService.update(id, details));

        verify(movieSearchCache, never()).invalidate(any());
        verify(movieByIdCache, never()).evictAll(any(), any());
    }

    @Test
    void delete_shouldThrow_whenDirectorMissing() {
        when(directorRepository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> directorService.delete(5L));

        verify(directorRepository, never()).deleteById(5L);
    }

    @Test
    void delete_shouldThrow_whenDirectorHasMovies() {
        when(directorRepository.existsById(5L)).thenReturn(true);
        when(movieRepository.existsByDirectorId(5L)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> directorService.delete(5L));

        verify(directorRepository, never()).deleteById(5L);
    }

    @Test
    void delete_shouldDelete_whenNoRelatedMovies() {
        when(directorRepository.existsById(5L)).thenReturn(true);
        when(movieRepository.existsByDirectorId(5L)).thenReturn(false);

        directorService.delete(5L);

        verify(directorRepository).deleteById(5L);
    }

    private Director director(Long id, String lastName, String firstName, String middleName) {
        Director director = new Director();
        director.setId(id);
        director.setLastName(lastName);
        director.setFirstName(firstName);
        director.setMiddleName(middleName);
        return director;
    }
}
