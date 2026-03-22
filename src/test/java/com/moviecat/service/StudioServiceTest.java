package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Studio;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.StudioRepository;
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
class StudioServiceTest {

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    private StudioService studioService;

    @BeforeEach
    void setUp() {
        studioService = new StudioService(studioRepository, movieRepository, movieByIdCache, movieSearchCache);
    }

    @Test
    void getAll_shouldNormalizePagingAndSorting() {
        PageRequest expectedRequest = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(studioRepository.findAll(expectedRequest)).thenReturn(Page.empty());

        studioService.getAll(-10, 0, "unsupported", "wrong");

        verify(studioRepository).findAll(expectedRequest);
    }

    @Test
    void getById_shouldReturnStudio_whenExists() {
        Studio studio = studio(1L, "WB", "LA");
        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));

        Studio result = studioService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("WB", result.getTitle());
    }

    @Test
    void getById_shouldThrow_whenMissing() {
        when(studioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studioService.getById(1L));
    }

    @Test
    void create_shouldSaveStudio_whenUnique() {
        Studio input = studio(null, "WB", "LA");
        Studio savedStudio = studio(6L, "WB", "LA");
        when(studioRepository.existsByTitle("WB")).thenReturn(false);
        when(studioRepository.save(input)).thenReturn(savedStudio);

        Studio result = studioService.create(input);

        assertEquals(6L, result.getId());
        assertEquals("WB", result.getTitle());
    }

    @Test
    void create_shouldThrow_whenDuplicateTitle() {
        Studio input = studio(null, "WB", "LA");
        when(studioRepository.existsByTitle("WB")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> studioService.create(input));
        verify(studioRepository, never()).save(input);
    }

    @Test
    void update_shouldUpdateStudioAndInvalidateCaches() {
        Long id = 9L;
        Studio existing = studio(id, "Old", "Minsk");
        Studio details = studio(null, "New", "LA");

        when(studioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(studioRepository.existsByTitleAndIdNot("New", id)).thenReturn(false);
        when(movieRepository.findIdsByStudioId(id)).thenReturn(List.of(101L, 202L));
        when(studioRepository.save(existing)).thenReturn(existing);

        Studio result = studioService.update(id, details);

        assertEquals("New", result.getTitle());
        assertEquals("LA", result.getAddress());
        verify(movieSearchCache).invalidate("StudioService.update id=9");
        verify(movieByIdCache).evictAll(List.of(101L, 202L), "StudioService.update id=9");
    }

    @Test
    void update_shouldThrow_whenDuplicateTitle() {
        Long id = 9L;
        Studio existing = studio(id, "Old", "Minsk");
        Studio details = studio(null, "New", "LA");

        when(studioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(studioRepository.existsByTitleAndIdNot("New", id)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> studioService.update(id, details));

        verify(movieSearchCache, never()).invalidate(any());
        verify(movieByIdCache, never()).evictAll(any(), any());
    }

    @Test
    void delete_shouldThrow_whenStudioMissing() {
        when(studioRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> studioService.delete(2L));
    }

    @Test
    void delete_shouldThrow_whenStudioHasMovies() {
        when(studioRepository.existsById(2L)).thenReturn(true);
        when(movieRepository.existsByStudioId(2L)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> studioService.delete(2L));

        verify(studioRepository, never()).deleteById(2L);
    }

    @Test
    void delete_shouldDelete_whenNoRelatedMovies() {
        when(studioRepository.existsById(2L)).thenReturn(true);
        when(movieRepository.existsByStudioId(2L)).thenReturn(false);

        studioService.delete(2L);

        verify(studioRepository).deleteById(2L);
    }

    private Studio studio(Long id, String title, String address) {
        Studio studio = new Studio();
        studio.setId(id);
        studio.setTitle(title);
        studio.setAddress(address);
        return studio;
    }
}
