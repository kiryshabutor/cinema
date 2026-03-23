package com.moviecat.service;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieSearchParams;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.mapper.MovieMapper;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private static final String MOVIE_NOT_FOUND_MSG = "Movie not found with id: ";
    private static final String DIRECTOR_NOT_FOUND_MSG = "Director not found";
    private static final String STUDIO_NOT_FOUND_MSG = "Studio not found";
    private static final String GENRES_NOT_FOUND_MSG = "Genres not found with ids: ";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "title";
    private static final String DEFAULT_DIRECTION = "asc";
    private static final String DESC_DIRECTION = "desc";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(DEFAULT_SORT_FIELD, "year", "viewCount", "id");

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final StudioRepository studioRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;
    private final MovieByIdCache movieByIdCache;
    private final MovieSearchCache movieSearchCache;
    private final ObjectProvider<MovieService> movieServiceProvider;

    public String uploadPoster(@NonNull Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        String filename = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + filename;

        movie.setPosterUrl(fileUrl);
        movieRepository.save(movie);

        invalidateCaches("MovieService.uploadPoster movieId=" + id);
        return fileUrl;
    }

    public List<MovieResponseDto> getAll() {
        return movieRepository.findAllWithDetails().stream()
                .map(MovieMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getAllPaged(
            int page,
            int size,
            String sort,
            String direction,
            boolean nativeQuery) {
        return movieServiceProvider.getObject()
                .searchAdvanced(new MovieSearchParams("", "", "", "", page, size, sort, direction, nativeQuery));
    }

    public List<MovieResponseDto> getAllNPlusOneDemo() {
        return movieRepository.findAll().stream()
                .map(MovieMapper::toResponseDto)
                .toList();
    }

    public MovieResponseDto getById(@NonNull Long id) {
        MovieResponseDto cachedMovie = movieByIdCache.get(id);
        if (cachedMovie != null) {
            log.info("MOVIE BY ID CACHE HIT: movieId={}", id);
            return cachedMovie;
        }

        log.info("MOVIE BY ID CACHE MISS: movieId={}", id);
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));
        MovieResponseDto loadedMovie = MovieMapper.toResponseDto(movie);
        movieByIdCache.put(id, loadedMovie);
        return loadedMovie;
    }

    @Transactional(readOnly = true)
    public Page<MovieResponseDto> searchAdvanced(MovieSearchParams params) {
        MovieSearchParams searchParams = Objects.requireNonNull(params, "params");

        String normalizedTitle = normalizeTitle(searchParams.getTitle());
        String normalizedDirectorLastName = normalizeTextFilter(searchParams.getDirectorLastName());
        String normalizedGenreName = normalizeTextFilter(searchParams.getGenreName());
        String normalizedStudioTitle = normalizeTextFilter(searchParams.getStudioTitle());
        int normalizedPage = PagingSortingUtils.normalizePage(searchParams.getPage(), DEFAULT_PAGE);
        int normalizedSize = PagingSortingUtils.normalizeSize(searchParams.getSize(), DEFAULT_SIZE, MAX_SIZE);
        String normalizedSort = PagingSortingUtils.normalizeSort(
                searchParams.getSort(), DEFAULT_SORT_FIELD, ALLOWED_SORT_FIELDS, true);
        String normalizedDirection = PagingSortingUtils.normalizeDirection(
                searchParams.getDirection(), DEFAULT_DIRECTION, DESC_DIRECTION);
        boolean nativeQuery = searchParams.isNativeQuery();

        MovieSearchKey key = new MovieSearchKey(
                normalizedTitle,
                normalizedDirectorLastName,
                normalizedGenreName,
                normalizedStudioTitle,
                new MovieSearchKey.PagingOptions(
                        normalizedPage,
                        normalizedSize,
                        normalizedSort,
                        normalizedDirection),
                nativeQuery);

        Page<MovieResponseDto> cachedPage = movieSearchCache.get(key);
        if (cachedPage != null) {
            log.info(
                    "CACHE HIT: title='{}', director='{}', genre='{}', studio='{}', page={}, size={}, sort='{}',"
                            + " direction='{}', native={}",
                    normalizedTitle,
                    normalizedDirectorLastName,
                    normalizedGenreName,
                    normalizedStudioTitle,
                    normalizedPage,
                    normalizedSize,
                    normalizedSort,
                    normalizedDirection,
                    nativeQuery);
            return cachedPage;
        }

        log.info(
                "CACHE MISS: title='{}', director='{}', genre='{}', studio='{}', page={}, size={}, "
                        + "sort='{}', direction='{}', native={}",
                normalizedTitle,
                normalizedDirectorLastName,
                normalizedGenreName,
                normalizedStudioTitle,
                normalizedPage,
                normalizedSize,
                normalizedSort,
                normalizedDirection,
                nativeQuery);
        Page<MovieResponseDto> loadedPage = loadSearchPage(key);
        movieSearchCache.put(key, loadedPage);
        return loadedPage;
    }

    @Transactional
    public MovieResponseDto create(MovieCreateDto dto) {
        MovieResponseDto createdMovie = createMovieInternal(dto);
        invalidateCaches("MovieService.create");
        return createdMovie;
    }

    private MovieResponseDto createMovieInternal(MovieCreateDto dto) {
        if (movieRepository.existsByTitle(dto.getTitle())) {
            throw new ResourceAlreadyExistsException("Movie with title '" + dto.getTitle() + "' already exists");
        }
        Movie movie = MovieMapper.toEntity(dto);

        if (dto.getDirectorId() != null) {
            Long directorId = dto.getDirectorId();
            Director director = directorRepository.findById(Objects.requireNonNull(directorId))
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Long studioId = dto.getStudioId();
            Studio studio = studioRepository.findById(Objects.requireNonNull(studioId))
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            movie.setGenres(resolveGenresByIds(dto.getGenreIds()));
        }

        Movie savedMovie = movieRepository.save(Objects.requireNonNull(movie, "movie"));
        return MovieMapper.toResponseDto(savedMovie);
    }

    @Transactional
    public MovieResponseDto update(@NonNull Long id, MovieUpdateDto dto) {
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        if (!movie.getTitle().equals(dto.getTitle()) && movieRepository.existsByTitle(dto.getTitle())) {
            throw new ResourceAlreadyExistsException("Movie with title '" + dto.getTitle() + "' already exists");
        }

        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setDuration(dto.getDuration());
        movie.setViewCount(dto.getViewCount());

        if (dto.getDirectorId() != null) {
            Long directorId = dto.getDirectorId();
            Director director = directorRepository.findById(Objects.requireNonNull(directorId))
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        } else {
            movie.setDirector(null);
        }

        if (dto.getStudioId() != null) {
            Long studioId = dto.getStudioId();
            Studio studio = studioRepository.findById(Objects.requireNonNull(studioId))
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        } else {
            movie.setStudio(null);
        }

        if (dto.getGenreIds() != null) {
            movie.setGenres(resolveGenresByIds(dto.getGenreIds()));
        } else {
            movie.getGenres().clear();
        }

        Movie updatedMovie = movieRepository.save(movie);
        invalidateCaches("MovieService.update movieId=" + id);
        return MovieMapper.toResponseDto(updatedMovie);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id);
        }
        movieRepository.deleteById(id);
        invalidateCaches("MovieService.delete movieId=" + id);
    }

    @Transactional
    public MovieResponseDto patch(@NonNull Long id, MoviePatchDto dto) {
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        MovieMapper.updateEntity(movie, dto);

        if (dto.getDirectorId() != null) {
            Long directorId = dto.getDirectorId();
            Director director = directorRepository.findById(Objects.requireNonNull(directorId))
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Long studioId = dto.getStudioId();
            Studio studio = studioRepository.findById(Objects.requireNonNull(studioId))
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        }

        if (dto.getGenreIds() != null) {
            movie.setGenres(resolveGenresByIds(dto.getGenreIds()));
        }

        Movie updatedMovie = movieRepository.save(Objects.requireNonNull(movie, "movie"));
        invalidateCaches("MovieService.patch movieId=" + id);
        return MovieMapper.toResponseDto(updatedMovie);
    }

    private void invalidateCaches(String reason) {
        movieSearchCache.invalidate(reason);
        movieByIdCache.invalidate(reason);
    }

    private Set<Genre> resolveGenresByIds(Set<Long> genreIds) {
        Set<Long> requestedIds = Objects.requireNonNull(genreIds, "genreIds");
        Set<Genre> resolvedGenres = new HashSet<>(genreRepository.findAllById(requestedIds));
        Set<Long> foundIds = resolvedGenres.stream()
                .map(Genre::getId)
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> missingIds = new TreeSet<>(requestedIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(GENRES_NOT_FOUND_MSG + missingIds);
        }
        return resolvedGenres;
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        String normalizedTitle = title.trim().toLowerCase(Locale.ROOT);
        if (normalizedTitle.isEmpty()) {
            return "";
        }
        return normalizedTitle;
    }

    private String normalizeTextFilter(String value) {
        if (value == null) {
            return "";
        }
        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        if (normalizedValue.isEmpty()) {
            return "";
        }
        return normalizedValue;
    }

    private Page<MovieResponseDto> loadSearchPage(MovieSearchKey key) {
        String normalizedTitle = key.titleNormalized();
        String normalizedDirectorLastName = key.directorLastNameNormalized();
        String normalizedGenreName = key.genreNameNormalized();
        String normalizedStudioTitle = key.studioTitleNormalized();
        int normalizedPage = key.pagingOptions().page();
        int normalizedSize = key.pagingOptions().size();
        String normalizedSort = key.pagingOptions().sort();
        String normalizedDirection = key.pagingOptions().direction();
        boolean nativeQuery = key.nativeQuery();

        Pageable pageable;
        Page<MovieRepository.MovieSearchRowProjection> movieSearchPage;
        if (nativeQuery) {
            pageable = PageRequest.of(normalizedPage, normalizedSize);
            movieSearchPage = movieRepository.searchAdvancedNative(
                    normalizedTitle,
                    normalizedDirectorLastName,
                    normalizedGenreName,
                    normalizedStudioTitle,
                    normalizedSort,
                    normalizedDirection,
                    pageable);
        } else {
            pageable = PageRequest.of(
                    normalizedPage,
                    normalizedSize,
                    PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
            movieSearchPage = movieRepository.searchAdvancedJpql(
                    normalizedTitle,
                    normalizedDirectorLastName,
                    normalizedGenreName,
                    normalizedStudioTitle,
                    pageable);
        }
        return movieSearchPage.map(MovieService::toSearchResponseDto);
    }

    private static MovieResponseDto toSearchResponseDto(MovieRepository.MovieSearchRowProjection row) {
        MovieResponseDto dto = new MovieResponseDto();
        dto.setId(row.getId());
        dto.setTitle(row.getTitle());
        dto.setYear(row.getYear());
        dto.setDuration(row.getDuration());
        dto.setViewCount(row.getViewCount());
        dto.setPosterUrl(row.getPosterUrl());
        dto.setDirectorId(row.getDirectorId());
        dto.setDirectorLastName(row.getDirectorLastName());
        dto.setDirectorFirstName(row.getDirectorFirstName());
        dto.setDirectorMiddleName(row.getDirectorMiddleName());
        dto.setStudioId(row.getStudioId());
        dto.setStudioTitle(row.getStudioTitle());
        dto.setGenres(List.of());
        return dto;
    }
}
