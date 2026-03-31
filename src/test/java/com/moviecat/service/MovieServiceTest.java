package com.moviecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieSearchParams;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.dto.ViewCountResponseDto;
import com.moviecat.dto.ViewRaceDemoResponseDto;
import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Director;
import com.moviecat.model.Genre;
import com.moviecat.model.Movie;
import com.moviecat.model.Studio;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.StudioRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import com.moviecat.service.cache.MovieSearchKey;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MovieByIdCache movieByIdCache;

    @Mock
    private MovieSearchCache movieSearchCache;

    @Mock
    private ObjectProvider<MovieService> movieServiceProvider;

    @Mock
    private MultipartFile multipartFile;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(
                movieRepository,
                directorRepository,
                studioRepository,
                genreRepository,
                fileStorageService,
                movieByIdCache,
                movieSearchCache,
                movieServiceProvider);
    }

    @Test
    void getById_shouldReturnCachedMovie_whenCacheHit() {
        MovieResponseDto cached = new MovieResponseDto();
        cached.setId(1L);
        when(movieByIdCache.get(1L)).thenReturn(cached);

        MovieResponseDto result = movieService.getById(1L);

        assertSame(cached, result);
        verify(movieRepository, never()).findByIdWithDetails(anyLong());
    }

    @Test
    void getById_shouldLoadAndCacheMovie_whenCacheMiss() {
        when(movieByIdCache.get(1L)).thenReturn(null);
        Movie movie = movie(1L, "Interstellar");
        when(movieRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(movie));

        MovieResponseDto result = movieService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Interstellar", result.getTitle());
        verify(movieByIdCache).put(eq(1L), any(MovieResponseDto.class));
    }

    @Test
    void getById_shouldThrow_whenMovieNotFound() {
        when(movieByIdCache.get(11L)).thenReturn(null);
        when(movieRepository.findByIdWithDetails(11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getById(11L));
    }

    @Test
    void getAll_shouldReturnMappedMovies() {
        Movie movie = movie(1L, "Interstellar");
        when(movieRepository.findAllWithDetails()).thenReturn(nn(List.of(movie)));

        List<MovieResponseDto> result = movieService.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Interstellar", result.get(0).getTitle());
    }

    @Test
    void getAllNPlusOneDemo_shouldReturnMappedMovies() {
        Movie movie = movie(2L, "Memento");
        when(movieRepository.findAll()).thenReturn(nn(List.of(movie)));

        List<MovieResponseDto> result = movieService.getAllNPlusOneDemo();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("Memento", result.get(0).getTitle());
    }

    @Test
    void searchAdvanced_shouldReturnCachedPage_whenCacheHit() {
        MovieSearchParams params = new MovieSearchParams("title", "", "", "", 0, 10, "title", "asc", false);
        Page<MovieResponseDto> cached = new PageImpl<>(nn(List.of(new MovieResponseDto())));
        when(movieSearchCache.get(any(MovieSearchKey.class))).thenReturn(cached);

        Page<MovieResponseDto> result = movieService.searchAdvanced(params);

        assertSame(cached, result);
        verify(movieRepository, never()).searchAdvancedJpql(anyString(), anyString(), anyString(), anyString(), any());
        verify(movieRepository, never()).searchAdvancedNative(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any());
    }

    @Test
    void searchAdvanced_shouldLoadWithJpqlAndCache_whenCacheMiss() {
        MovieSearchParams params = new MovieSearchParams("  TITle ", " Nolan ", " sci-fi ", " warner ", -3, 500,
                "YEAR", "DESC", false);
        when(movieSearchCache.get(any(MovieSearchKey.class))).thenReturn(null);

        MovieRepository.MovieSearchRowProjection row = projection(7L, "Interstellar");
        Page<MovieRepository.MovieSearchRowProjection> repoPage = new PageImpl<>(nn(List.of(row)));
        when(movieRepository.searchAdvancedJpql(anyString(), anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(repoPage);

        Page<MovieResponseDto> result = movieService.searchAdvanced(params);

        assertEquals(1, result.getContent().size());
        assertEquals("Interstellar", result.getContent().get(0).getTitle());

        ArgumentCaptor<MovieSearchKey> keyCaptor = ArgumentCaptor.forClass(MovieSearchKey.class);
        verify(movieSearchCache).get(keyCaptor.capture());
        MovieSearchKey key = keyCaptor.getValue();
        assertEquals("title", key.titleNormalized());
        assertEquals("nolan", key.directorLastNameNormalized());
        assertEquals("sci-fi", key.genreNameNormalized());
        assertEquals("warner", key.studioTitleNormalized());
        assertEquals(0, key.pagingOptions().page());
        assertEquals(100, key.pagingOptions().size());
        assertEquals("year", key.pagingOptions().sort());
        assertEquals("desc", key.pagingOptions().direction());

        verify(movieSearchCache).put(any(MovieSearchKey.class), eq(result));
        verify(movieRepository).searchAdvancedJpql(anyString(), anyString(), anyString(), anyString(), any(Pageable.class));
        verify(movieRepository, never()).searchAdvancedNative(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any());
    }

    @Test
    void searchAdvanced_shouldUseNativeQuery_whenRequested() {
        MovieSearchParams params = new MovieSearchParams("", "", "", "", 1, 20, "id", "asc", true);
        when(movieSearchCache.get(any(MovieSearchKey.class))).thenReturn(null);

        MovieRepository.MovieSearchRowProjection row = projection(3L, "Tenet");
        Page<MovieRepository.MovieSearchRowProjection> repoPage = new PageImpl<>(nn(List.of(row)));
        when(movieRepository.searchAdvancedNative(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), any(Pageable.class))).thenReturn(repoPage);

        Page<MovieResponseDto> result = movieService.searchAdvanced(params);

        assertEquals(1, result.getTotalElements());
        verify(movieRepository).searchAdvancedNative(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), any(Pageable.class));
        verify(movieRepository, never()).searchAdvancedJpql(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void searchAdvanced_shouldNormalizeNullFilters() {
        MovieSearchParams params = new MovieSearchParams(null, null, null, null, 0, 10, "title", "asc", false);
        when(movieSearchCache.get(any(MovieSearchKey.class))).thenReturn(null);
        when(movieRepository.searchAdvancedJpql(anyString(), anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        movieService.searchAdvanced(params);

        ArgumentCaptor<MovieSearchKey> keyCaptor = ArgumentCaptor.forClass(MovieSearchKey.class);
        verify(movieSearchCache).get(keyCaptor.capture());
        MovieSearchKey key = keyCaptor.getValue();
        assertEquals("", key.titleNormalized());
        assertEquals("", key.directorLastNameNormalized());
        assertEquals("", key.genreNameNormalized());
        assertEquals("", key.studioTitleNormalized());
    }

    @Test
    void create_shouldPersistMovieAndInvalidateCaches() {
        MovieCreateDto dto = createDto();
        dto.setDirectorId(11L);
        dto.setStudioId(22L);
        Set<Long> genreIds = nn(Set.of(1L, 2L));
        dto.setGenreIds(genreIds);

        Director director = new Director();
        director.setId(11L);
        director.setLastName("Nolan");
        Studio studio = new Studio();
        studio.setId(22L);
        studio.setTitle("Warner Bros");
        Genre g1 = new Genre();
        g1.setId(1L);
        g1.setName("Sci-Fi");
        Genre g2 = new Genre();
        g2.setId(2L);
        g2.setName("Drama");
        Movie savedFromRepo = movie(100L, dto.getTitle());
        savedFromRepo.setYear(dto.getYear());
        savedFromRepo.setDuration(dto.getDuration());
        savedFromRepo.setViewCount(dto.getViewCount());
        savedFromRepo.setDirector(director);
        savedFromRepo.setStudio(studio);
        savedFromRepo.setGenres(new HashSet<>(nn(List.of(g1, g2))));

        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(directorRepository.findById(11L)).thenReturn(Optional.of(director));
        when(studioRepository.findById(22L)).thenReturn(Optional.of(studio));
        when(genreRepository.findAllById(genreIds)).thenReturn(nn(List.of(g1, g2)));
        when(movieRepository.save(any(Movie.class))).thenReturn(Objects.requireNonNull(savedFromRepo));

        MovieResponseDto result = movieService.create(dto);

        assertEquals(100L, result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals(11L, result.getDirectorId());
        assertEquals(22L, result.getStudioId());
        assertEquals(2, result.getGenres().size());

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie savedMovie = Objects.requireNonNull(movieCaptor.getValue());
        assertSame(director, savedMovie.getDirector());
        assertSame(studio, savedMovie.getStudio());
        assertEquals(2, savedMovie.getGenres().size());

        verify(movieSearchCache).invalidate("MovieService.create");
        verify(movieByIdCache).invalidate("MovieService.create");
    }

    @Test
    void create_shouldThrow_whenTitleAlreadyExists() {
        MovieCreateDto dto = createDto();
        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> movieService.create(dto));

        verify(movieSearchCache, never()).invalidate(anyString());
        verify(movieByIdCache, never()).invalidate(anyString());
    }

    @Test
    void create_shouldThrow_whenDirectorMissing() {
        MovieCreateDto dto = createDto();
        dto.setDirectorId(99L);
        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(directorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.create(dto));
    }

    @Test
    void create_shouldThrow_whenStudioMissing() {
        MovieCreateDto dto = createDto();
        dto.setStudioId(77L);
        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(studioRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.create(dto));
    }

    @Test
    void create_shouldSkipGenresLookup_whenGenreIdsEmpty() {
        MovieCreateDto dto = createDto();
        dto.setGenreIds(Set.of());

        Movie saved = movie(200L, dto.getTitle());
        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        MovieResponseDto result = movieService.create(dto);

        assertEquals(200L, result.getId());
        verify(genreRepository, never()).findAllById(any());
    }

    @Test
    void create_shouldSkipGenresLookup_whenGenreIdsNull() {
        MovieCreateDto dto = createDto();
        dto.setGenreIds(null);

        Movie saved = movie(201L, dto.getTitle());
        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        MovieResponseDto result = movieService.create(dto);

        assertEquals(201L, result.getId());
        verify(genreRepository, never()).findAllById(any());
    }

    @Test
    void create_shouldThrow_whenAnyGenreIdMissing() {
        MovieCreateDto dto = createDto();
        Set<Long> genreIds = nn(Set.of(1L, 3000L));
        dto.setGenreIds(genreIds);

        Genre existingGenre = new Genre();
        existingGenre.setId(1L);
        existingGenre.setName("Drama");

        when(movieRepository.existsByTitle(dto.getTitle())).thenReturn(false);
        when(genreRepository.findAllById(genreIds)).thenReturn(nn(List.of(existingGenre)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> movieService.create(dto));

        assertTrue(exception.getMessage().contains("3000"));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void update_shouldReplaceFieldsAndInvalidateCaches() {
        Long movieId = 10L;
        Movie existing = movie(movieId, "Old");
        Director oldDirector = new Director();
        oldDirector.setId(1L);
        existing.setDirector(oldDirector);
        Studio oldStudio = new Studio();
        oldStudio.setId(1L);
        existing.setStudio(oldStudio);
        Genre oldGenre = new Genre();
        oldGenre.setId(1L);
        existing.getGenres().add(oldGenre);

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("New Title");
        dto.setYear(2025);
        dto.setDuration(130);
        dto.setViewCount(20L);
        dto.setDirectorId(null);
        dto.setStudioId(null);
        dto.setGenreIds(null);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("New Title")).thenReturn(false);
        when(movieRepository.save(existing)).thenReturn(existing);

        MovieResponseDto result = movieService.update(movieId, dto);

        assertEquals("New Title", result.getTitle());
        assertNull(existing.getDirector());
        assertNull(existing.getStudio());
        assertTrue(existing.getGenres().isEmpty());
        verify(movieSearchCache).invalidate("MovieService.update movieId=10");
        verify(movieByIdCache).invalidate("MovieService.update movieId=10");
    }

    @Test
    void update_shouldSetDirectorStudioAndGenres_whenIdsProvided() {
        Long movieId = 12L;
        Movie existing = movie(movieId, "Old");

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Updated");
        dto.setYear(2025);
        dto.setDuration(121);
        dto.setViewCount(42L);
        dto.setDirectorId(5L);
        dto.setStudioId(6L);
        Set<Long> genreIds = nn(Set.of(7L));
        dto.setGenreIds(genreIds);

        Director director = new Director();
        director.setId(5L);
        Studio studio = new Studio();
        studio.setId(6L);
        Genre genre = new Genre();
        genre.setId(7L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("Updated")).thenReturn(false);
        when(directorRepository.findById(5L)).thenReturn(Optional.of(director));
        when(studioRepository.findById(6L)).thenReturn(Optional.of(studio));
        when(genreRepository.findAllById(genreIds)).thenReturn(nn(List.of(genre)));
        when(movieRepository.save(existing)).thenReturn(existing);

        MovieResponseDto result = movieService.update(movieId, dto);

        assertEquals("Updated", result.getTitle());
        assertSame(director, existing.getDirector());
        assertSame(studio, existing.getStudio());
        assertEquals(1, existing.getGenres().size());
    }

    @Test
    void update_shouldNotCheckTitleUniqueness_whenTitleUnchanged() {
        Long movieId = 13L;
        Movie existing = movie(movieId, "Same title");

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Same title");
        dto.setYear(2020);
        dto.setDuration(111);
        dto.setViewCount(3L);
        dto.setDirectorId(null);
        dto.setStudioId(null);
        dto.setGenreIds(null);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.save(existing)).thenReturn(existing);

        movieService.update(movieId, dto);

        verify(movieRepository, never()).existsByTitle(anyString());
    }

    @Test
    void update_shouldThrow_whenMovieMissing() {
        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Title");
        dto.setYear(2020);
        dto.setDuration(100);
        dto.setViewCount(1L);

        when(movieRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.update(404L, dto));
    }

    @Test
    void update_shouldThrow_whenDirectorMissingAndDirectorIdProvided() {
        Long movieId = 14L;
        Movie existing = movie(movieId, "Old");

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Updated");
        dto.setYear(2020);
        dto.setDuration(101);
        dto.setViewCount(2L);
        dto.setDirectorId(55L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("Updated")).thenReturn(false);
        when(directorRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.update(movieId, dto));
    }

    @Test
    void update_shouldThrow_whenStudioMissingAndStudioIdProvided() {
        Long movieId = 15L;
        Movie existing = movie(movieId, "Old");

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Updated");
        dto.setYear(2020);
        dto.setDuration(101);
        dto.setViewCount(2L);
        dto.setStudioId(66L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("Updated")).thenReturn(false);
        when(studioRepository.findById(66L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.update(movieId, dto));
    }

    @Test
    void update_shouldThrow_whenAnyGenreIdMissing() {
        Long movieId = 16L;
        Movie existing = movie(movieId, "Old");

        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Updated");
        dto.setYear(2020);
        dto.setDuration(101);
        dto.setViewCount(2L);
        Set<Long> genreIds = nn(Set.of(7L, 3000L));
        dto.setGenreIds(genreIds);

        Genre existingGenre = new Genre();
        existingGenre.setId(7L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("Updated")).thenReturn(false);
        when(genreRepository.findAllById(genreIds)).thenReturn(nn(List.of(existingGenre)));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> movieService.update(movieId, dto));

        assertTrue(exception.getMessage().contains("3000"));
        verify(movieRepository, never()).save(existing);
    }

    @Test
    void update_shouldThrow_whenNewTitleAlreadyExists() {
        Long movieId = 10L;
        Movie existing = movie(movieId, "Old");
        MovieUpdateDto dto = new MovieUpdateDto();
        dto.setTitle("Duplicated");
        dto.setYear(2022);
        dto.setDuration(120);
        dto.setViewCount(1L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("Duplicated")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> movieService.update(movieId, dto));

        verify(movieSearchCache, never()).invalidate(anyString());
        verify(movieByIdCache, never()).invalidate(anyString());
    }

    @Test
    void patch_shouldApplyProvidedFieldsAndInvalidateCaches() {
        Long movieId = 10L;
        Movie existing = movie(movieId, "Matrix");
        existing.setYear(1999);
        existing.setDuration(130);
        existing.setViewCount(5L);

        Director director = new Director();
        director.setId(5L);
        director.setLastName("Wachowski");
        Studio studio = new Studio();
        studio.setId(7L);
        studio.setTitle("WB");
        Genre genre = new Genre();
        genre.setId(9L);
        genre.setName("Action");

        MoviePatchDto dto = new MoviePatchDto();
        dto.setYear(2000);
        dto.setDirectorId(5L);
        dto.setStudioId(7L);
        Set<Long> patchGenreIds = nn(Set.of(9L));
        dto.setGenreIds(patchGenreIds);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(directorRepository.findById(5L)).thenReturn(Optional.of(director));
        when(studioRepository.findById(7L)).thenReturn(Optional.of(studio));
        when(genreRepository.findAllById(patchGenreIds)).thenReturn(nn(List.of(genre)));
        when(movieRepository.save(existing)).thenReturn(existing);

        MovieResponseDto result = movieService.patch(movieId, dto);

        assertEquals(2000, result.getYear());
        assertEquals(5L, result.getDirectorId());
        assertEquals(7L, result.getStudioId());
        assertEquals(1, result.getGenres().size());
        verify(movieSearchCache).invalidate("MovieService.patch movieId=10");
        verify(movieByIdCache).invalidate("MovieService.patch movieId=10");
    }

    @Test
    void patch_shouldKeepExistingRelations_whenIdsAreNotProvided() {
        Long movieId = 11L;
        Movie existing = movie(movieId, "Matrix");
        Director director = new Director();
        director.setId(1L);
        Studio studio = new Studio();
        studio.setId(2L);
        Genre genre = new Genre();
        genre.setId(3L);
        existing.setDirector(director);
        existing.setStudio(studio);
        existing.setGenres(new HashSet<>(nn(List.of(genre))));

        MoviePatchDto dto = new MoviePatchDto();
        dto.setYear(2001);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(movieRepository.save(existing)).thenReturn(existing);

        movieService.patch(movieId, dto);

        assertSame(director, existing.getDirector());
        assertSame(studio, existing.getStudio());
        assertEquals(1, existing.getGenres().size());
    }

    @Test
    void patch_shouldThrow_whenMovieMissing() {
        when(movieRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());
        MoviePatchDto dto = new MoviePatchDto();

        assertThrows(ResourceNotFoundException.class, () -> movieService.patch(404L, dto));
    }

    @Test
    void patch_shouldThrow_whenDirectorMissingAndDirectorIdProvided() {
        Long movieId = 12L;
        Movie existing = movie(movieId, "Patch");
        MoviePatchDto dto = new MoviePatchDto();
        dto.setDirectorId(10L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(directorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.patch(movieId, dto));
    }

    @Test
    void patch_shouldThrow_whenStudioMissingAndStudioIdProvided() {
        Long movieId = 13L;
        Movie existing = movie(movieId, "Patch");
        MoviePatchDto dto = new MoviePatchDto();
        dto.setStudioId(20L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(studioRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.patch(movieId, dto));
    }

    @Test
    void patch_shouldThrow_whenAnyGenreIdMissing() {
        Long movieId = 14L;
        Movie existing = movie(movieId, "Patch");
        MoviePatchDto dto = new MoviePatchDto();
        Set<Long> genreIds = nn(Set.of(9L, 3000L));
        dto.setGenreIds(genreIds);

        Genre existingGenre = new Genre();
        existingGenre.setId(9L);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(existing));
        when(genreRepository.findAllById(genreIds)).thenReturn(nn(List.of(existingGenre)));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> movieService.patch(movieId, dto));

        assertTrue(exception.getMessage().contains("3000"));
        verify(movieRepository, never()).save(existing);
    }

    @Test
    void delete_shouldThrow_whenMovieNotExists() {
        when(movieRepository.existsById(77L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> movieService.delete(77L));

        verify(movieRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteAndInvalidateCaches() {
        when(movieRepository.existsById(7L)).thenReturn(true);

        movieService.delete(7L);

        verify(movieRepository).deleteById(7L);
        verify(movieSearchCache).invalidate("MovieService.delete movieId=7");
        verify(movieByIdCache).invalidate("MovieService.delete movieId=7");
    }

    @Test
    void uploadPoster_shouldStoreFileAndInvalidateCaches() {
        Movie movie = movie(1L, "Interstellar");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(fileStorageService.storeFile(multipartFile)).thenReturn("poster.jpg");

        String result = movieService.uploadPoster(1L, multipartFile);

        assertEquals("/uploads/poster.jpg", result);
        assertEquals("/uploads/poster.jpg", movie.getPosterUrl());
        verify(movieRepository).save(movie);
        verify(movieSearchCache).invalidate("MovieService.uploadPoster movieId=1");
        verify(movieByIdCache).invalidate("MovieService.uploadPoster movieId=1");
    }

    @Test
    void uploadPoster_shouldThrow_whenMovieMissing() {
        when(movieRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.uploadPoster(55L, multipartFile));

        verify(fileStorageService, never()).storeFile(any(MultipartFile.class));
    }

    @Test
    void getAllPaged_shouldDelegateToProxiedSearchMethod() {
        MovieService spyService = spy(movieService);
        when(movieServiceProvider.getObject()).thenReturn(spyService);

        Page<MovieResponseDto> expected = new PageImpl<>(nn(List.of(new MovieResponseDto())));
        doReturn(expected).when(spyService).searchAdvanced(any(MovieSearchParams.class));

        Page<MovieResponseDto> result = spyService.getAllPaged(2, 15, "year", "desc", true);

        assertSame(expected, result);

        ArgumentCaptor<MovieSearchParams> paramsCaptor = ArgumentCaptor.forClass(MovieSearchParams.class);
        verify(spyService).searchAdvanced(paramsCaptor.capture());
        MovieSearchParams params = paramsCaptor.getValue();
        assertEquals("", params.getTitle());
        assertEquals(2, params.getPage());
        assertEquals(15, params.getSize());
        assertEquals("year", params.getSort());
        assertEquals("desc", params.getDirection());
        assertTrue(params.isNativeQuery());
    }

    @Test
    void incrementViewCount_shouldIncrementAndInvalidateCaches() {
        Movie movie = movie(7L, "Interstellar");
        movie.setViewCount(100L);
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);

        ViewCountResponseDto result = movieService.incrementViewCount(7L);

        assertEquals(7L, result.movieId());
        assertEquals(101L, result.viewCount());
        verify(movieRepository).save(movie);
        verify(movieSearchCache).invalidate("MovieService.incrementViewCount movieId=7");
        verify(movieByIdCache).invalidate("MovieService.incrementViewCount movieId=7");
    }

    @Test
    void incrementViewCount_shouldThrow_whenMovieNotFound() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.incrementViewCount(404L));

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void incrementViewCount_shouldTreatNullCurrentViewCountAsZero() {
        Movie movie = movie(8L, "Tenet");
        movie.setViewCount(null);
        when(movieRepository.findById(8L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);

        ViewCountResponseDto result = movieService.incrementViewCount(8L);

        assertEquals(8L, result.movieId());
        assertEquals(1L, result.viewCount());
    }

    @Test
    void incrementViewCount_shouldReturnZero_whenSavedViewCountIsNull() {
        Movie movie = movie(9L, "Dune");
        movie.setViewCount(5L);
        Movie savedMovie = movie(9L, "Dune");
        savedMovie.setViewCount(null);
        when(movieRepository.findById(9L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(savedMovie);

        ViewCountResponseDto result = movieService.incrementViewCount(9L);

        assertEquals(9L, result.movieId());
        assertEquals(0L, result.viewCount());
    }

    @Test
    void runViewRaceDemo_shouldThrow_whenModeIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> movieService.runViewRaceDemo(1L, "wrong", 50, 1000));
    }

    @Test
    void runViewRaceDemo_shouldThrow_whenModeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> movieService.runViewRaceDemo(1L, null, 50, 1000));
    }

    @Test
    void runViewRaceDemo_shouldThrow_whenMovieIsMissingBeforeStart() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.runViewRaceDemo(404L, "safe", 50, 1));
    }

    @Test
    void runViewRaceDemo_shouldNormalizeMode_whenSafeContainsSpacesAndUppercase() {
        AtomicLong persistedViewCount = new AtomicLong(0L);

        when(movieRepository.findById(11L)).thenAnswer(invocation -> {
            Movie movie = movie(11L, "Interstellar");
            movie.setViewCount(persistedViewCount.get());
            return Optional.of(movie);
        });
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movieToSave = invocation.getArgument(0);
            Long updatedViewCount = movieToSave.getViewCount();
            persistedViewCount.set(updatedViewCount != null ? updatedViewCount : 0L);
            return movieToSave;
        });

        ViewRaceDemoResponseDto result = movieService.runViewRaceDemo(11L, " SAFE ", 50, 1);

        assertEquals("safe", result.mode());
        assertEquals(50L, result.expectedCount());
        assertEquals(0L, result.lostUpdates());
    }

    @Test
    void runViewRaceDemo_shouldReturnResponseForUnsafeMode() {
        AtomicLong persistedViewCount = new AtomicLong(0L);

        when(movieRepository.findById(12L)).thenAnswer(invocation -> {
            Movie movie = movie(12L, "Interstellar");
            movie.setViewCount(persistedViewCount.get());
            return Optional.of(movie);
        });
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movieToSave = invocation.getArgument(0);
            Long updatedViewCount = movieToSave.getViewCount();
            persistedViewCount.set(updatedViewCount != null ? updatedViewCount : 0L);
            return movieToSave;
        });

        ViewRaceDemoResponseDto result = movieService.runViewRaceDemo(12L, "unsafe", 50, 1);

        assertEquals("unsafe", result.mode());
        assertEquals(50L, result.expectedCount());
        assertTrue(result.actualCount() <= result.expectedCount());
    }

    @Test
    void runViewRaceDemo_shouldRethrowRuntimeExceptionFromWorker() {
        AtomicLong invocationCount = new AtomicLong(0L);
        when(movieRepository.findById(13L)).thenAnswer(invocation -> {
            if (invocationCount.getAndIncrement() == 0L) {
                Movie movie = movie(13L, "Interstellar");
                movie.setViewCount(0L);
                return Optional.of(movie);
            }
            return Optional.empty();
        });

        assertThrows(ResourceNotFoundException.class, () -> movieService.runViewRaceDemo(13L, "safe", 50, 1));
    }

    @Test
    void runViewRaceDemo_shouldWrapNonRuntimeThrowableFromWorker() {
        AtomicLong invocationCount = new AtomicLong(0L);
        when(movieRepository.findById(14L)).thenAnswer(invocation -> {
            if (invocationCount.getAndIncrement() == 0L) {
                Movie movie = movie(14L, "Interstellar");
                movie.setViewCount(0L);
                return Optional.of(movie);
            }
            throw new AssertionError("boom");
        });

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> movieService.runViewRaceDemo(14L, "safe", 50, 1));

        assertEquals("Race demo failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof AssertionError);
    }

    @Test
    void runViewRaceDemo_shouldTreatNullInitialViewCountAsZero() {
        AtomicLong persistedViewCount = new AtomicLong(0L);
        AtomicBoolean firstRead = new AtomicBoolean(true);

        when(movieRepository.findById(15L)).thenAnswer(invocation -> {
            Movie movie = movie(15L, "Interstellar");
            if (firstRead.getAndSet(false)) {
                movie.setViewCount(null);
            } else {
                movie.setViewCount(persistedViewCount.get());
            }
            return Optional.of(movie);
        });
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movieToSave = invocation.getArgument(0);
            Long updatedViewCount = movieToSave.getViewCount();
            persistedViewCount.set(updatedViewCount != null ? updatedViewCount : 0L);
            return movieToSave;
        });

        ViewRaceDemoResponseDto result = movieService.runViewRaceDemo(15L, "safe", 50, 1);

        assertEquals(50L, result.expectedCount());
        assertEquals(50L, result.actualCount());
        assertEquals(0L, result.lostUpdates());
    }

    @Test
    void awaitRaceStart_shouldThrow_whenCurrentThreadInterrupted() {
        CountDownLatch startLatch = new CountDownLatch(1);

        Thread.currentThread().interrupt();
        try {
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(movieService, "awaitRaceStart", startLatch));

            assertEquals("Race demo was interrupted", exception.getMessage());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void waitForRaceCompletion_shouldThrow_whenFutureGetIsInterrupted() throws Exception {
        Future<?> interruptedFuture = org.mockito.Mockito.mock(Future.class);
        List<Future<?>> futures = List.of(interruptedFuture);
        when(interruptedFuture.get()).thenThrow(new InterruptedException("interrupted"));

        try {
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            movieService,
                            "waitForRaceCompletion",
                            futures));

            assertEquals("Race demo was interrupted", exception.getMessage());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void shutdownExecutor_shouldForceShutdown_whenAwaitTerminationReturnsFalse() throws Exception {
        ExecutorService executor = org.mockito.Mockito.mock(ExecutorService.class);
        when(executor.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(false);

        ReflectionTestUtils.invokeMethod(movieService, "shutdownExecutor", executor);

        verify(executor).shutdown();
        verify(executor).shutdownNow();
    }

    @Test
    void shutdownExecutor_shouldForceShutdown_whenAwaitTerminationIsInterrupted() throws Exception {
        ExecutorService executor = org.mockito.Mockito.mock(ExecutorService.class);
        when(executor.awaitTermination(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException("stop"));

        ReflectionTestUtils.invokeMethod(movieService, "shutdownExecutor", executor);

        verify(executor).shutdown();
        verify(executor).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void runViewRaceDemo_shouldReturnNoLostUpdatesInSafeMode() {
        AtomicLong persistedViewCount = new AtomicLong(10L);

        when(movieRepository.findById(7L)).thenAnswer(invocation -> {
            Movie movie = movie(7L, "Interstellar");
            movie.setViewCount(persistedViewCount.get());
            return Optional.of(movie);
        });
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movieToSave = invocation.getArgument(0);
            Long updatedViewCount = movieToSave.getViewCount();
            persistedViewCount.set(updatedViewCount != null ? updatedViewCount : 0L);
            return movieToSave;
        });

        ViewRaceDemoResponseDto result = movieService.runViewRaceDemo(7L, "safe", 50, 5);

        assertEquals(7L, result.movieId());
        assertEquals("safe", result.mode());
        assertEquals(50, result.threads());
        assertEquals(5, result.incrementsPerThread());
        assertEquals(250L, result.expectedCount());
        assertEquals(250L, result.actualCount());
        assertEquals(0L, result.lostUpdates());
        assertTrue(result.durationMs() >= 0);
        verify(movieSearchCache).invalidate("MovieService.runViewRaceDemo movieId=7 mode=safe");
        verify(movieByIdCache).invalidate("MovieService.runViewRaceDemo movieId=7 mode=safe");
    }

    private MovieCreateDto createDto() {
        MovieCreateDto dto = new MovieCreateDto();
        dto.setTitle("Inception");
        dto.setYear(2010);
        dto.setDuration(148);
        dto.setViewCount(1L);
        return dto;
    }

    private Movie movie(Long id, String title) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setYear(2014);
        movie.setDuration(169);
        movie.setViewCount(100L);
        return movie;
    }

    private static <T> T nn(T value) {
        return Objects.requireNonNull(value);
    }

    private MovieRepository.MovieSearchRowProjection projection(Long id, String title) {
        MovieRepository.MovieSearchRowProjection row = org.mockito.Mockito.mock(MovieRepository.MovieSearchRowProjection.class);
        when(row.getId()).thenReturn(id);
        when(row.getTitle()).thenReturn(title);
        when(row.getYear()).thenReturn(2014);
        when(row.getDuration()).thenReturn(169);
        when(row.getViewCount()).thenReturn(100L);
        when(row.getPosterUrl()).thenReturn("/uploads/poster.jpg");
        when(row.getDirectorId()).thenReturn(10L);
        when(row.getDirectorLastName()).thenReturn("Nolan");
        when(row.getDirectorFirstName()).thenReturn("Christopher");
        when(row.getDirectorMiddleName()).thenReturn(null);
        when(row.getStudioId()).thenReturn(20L);
        when(row.getStudioTitle()).thenReturn("Warner Bros");
        return row;
    }
}
