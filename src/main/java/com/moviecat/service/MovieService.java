package com.moviecat.service;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.dto.MovieSearchParams;
import com.moviecat.dto.MovieUpdateDto;
import com.moviecat.dto.ReviewDto;
import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.SimulatedFailureException;
import com.moviecat.mapper.MovieMapper;
import com.moviecat.mapper.ReviewMapper;
import com.moviecat.model.Director;
import com.moviecat.model.Genre;
import com.moviecat.model.Movie;
import com.moviecat.model.Review;
import com.moviecat.model.Studio;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.StudioRepository;
import com.moviecat.service.cache.MovieSearchCache;
import com.moviecat.service.cache.MovieSearchKey;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
    private final MovieSearchCache movieSearchCache;
    private final ObjectProvider<MovieService> movieServiceProvider;

    public String uploadPoster(@NonNull Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        String filename = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + filename;

        movie.setPosterUrl(fileUrl);
        movieRepository.save(movie);

        movieSearchCache.invalidate("MovieService.uploadPoster movieId=" + id);
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
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MOVIE_NOT_FOUND_MSG + id));
        return MovieMapper.toResponseDto(movie);
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
                "CACHE MISS: title='{}', director='{}', genre='{}', studio='{}', page={}, size={}, sort='{}', "
                        + "direction='{}', native={}",
                normalizedTitle,
                normalizedDirectorLastName,
                normalizedGenreName,
                normalizedStudioTitle,
                normalizedPage,
                normalizedSize,
                normalizedSort,
                normalizedDirection,
                nativeQuery);

        Page<Movie> moviePage;
        if (nativeQuery) {
            Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
            moviePage = movieRepository.searchAdvancedNative(
                    normalizedTitle,
                    normalizedDirectorLastName,
                    normalizedGenreName,
                    normalizedStudioTitle,
                    normalizedSort,
                    normalizedDirection,
                    pageable);
        } else {
            Pageable pageable = PageRequest.of(
                    normalizedPage,
                    normalizedSize,
                    PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
            moviePage = movieRepository.searchAdvancedJpql(
                    normalizedTitle,
                    normalizedDirectorLastName,
                    normalizedGenreName,
                    normalizedStudioTitle,
                    pageable);
        }

        Page<MovieResponseDto> responsePage = moviePage.map(MovieMapper::toResponseDto);
        movieSearchCache.put(key, responsePage);
        return responsePage;
    }

    @Transactional
    public MovieResponseDto create(MovieCreateDto dto) {
        MovieResponseDto createdMovie = createMovieInternal(dto);
        movieSearchCache.invalidate("MovieService.create");
        return createdMovie;
    }

    @Transactional
    public MovieResponseDto createWithReviewsTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        try {
            return createMovieWithReviewsInternal(dto, failOnPurpose);
        } finally {
            movieSearchCache.invalidate("MovieService.createWithReviewsTransactional");
        }
    }

    public MovieResponseDto createWithReviewsNonTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        try {
            return createMovieWithReviewsInternal(dto, failOnPurpose);
        } finally {
            movieSearchCache.invalidate("MovieService.createWithReviewsNonTransactional");
        }
    }

    private MovieResponseDto createMovieWithReviewsInternal(MovieCreateDto dto, boolean failOnPurpose) {
        MovieResponseDto createdMovie = createMovieInternal(dto);
        Long createdMovieId = Objects.requireNonNull(createdMovie.getId(), "Created movie ID is null");

        if (dto.getReviews() != null) {
            for (int i = 0; i < dto.getReviews().size(); i++) {
                ReviewDto reviewDto = dto.getReviews().get(i);
                Review review = ReviewMapper.toEntity(reviewDto);

                Movie movieEntity = movieRepository.findById(createdMovieId).orElseThrow();
                review.setMovie(movieEntity);

                if (failOnPurpose && i == dto.getReviews().size() - 1) {
                    throw new SimulatedFailureException("Simulated failure during review processing");
                }
                movieEntity.getReviews().add(review);
                movieRepository.save(movieEntity);
            }
        }

        return MovieMapper.toResponseDto(movieRepository.findById(createdMovieId).orElseThrow());
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
            Set<Long> genreIds = dto.getGenreIds();
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(Objects.requireNonNull(genreIds)));
            movie.setGenres(genres);
        }

        Movie savedMovie = movieRepository.save(Objects.requireNonNull(movie, "movie"));
        return MovieMapper.toResponseDto(savedMovie);
    }

    @Transactional
    public MovieResponseDto update(@NonNull Long id, MovieUpdateDto dto) {
        Movie movie = movieRepository.findById(id)
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
            Set<Long> genreIds = dto.getGenreIds();
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(Objects.requireNonNull(genreIds)));
            movie.setGenres(genres);
        } else {
            movie.getGenres().clear();
        }

        Movie updatedMovie = movieRepository.save(movie);
        movieSearchCache.invalidate("MovieService.update movieId=" + id);
        return MovieMapper.toResponseDto(updatedMovie);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id);
        }
        movieRepository.deleteById(id);
        movieSearchCache.invalidate("MovieService.delete movieId=" + id);
    }

    @Transactional
    public MovieResponseDto patch(@NonNull Long id, MoviePatchDto dto) {
        Movie movie = movieRepository.findById(id)
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
            Set<Long> genreIds = dto.getGenreIds();
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(Objects.requireNonNull(genreIds)));
            movie.setGenres(genres);
        }

        Movie updatedMovie = movieRepository.save(Objects.requireNonNull(movie, "movie"));
        movieSearchCache.invalidate("MovieService.patch movieId=" + id);
        return MovieMapper.toResponseDto(updatedMovie);
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
}
