package com.moviecat.service;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final String MOVIE_NOT_FOUND_MSG = "Movie not found with id: ";
    private static final String DIRECTOR_NOT_FOUND_MSG = "Director not found";
    private static final String STUDIO_NOT_FOUND_MSG = "Studio not found";

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final StudioRepository studioRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;

    public String uploadPoster(@NonNull Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        String filename = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + filename;
        
        movie.setPosterUrl(fileUrl);
        movieRepository.save(movie);
        
        return fileUrl;
    }

    public List<MovieResponseDto> getAll() {
        return movieRepository.findAllWithDetails().stream()
                .map(MovieMapper::toResponseDto)
                .toList();
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

    public List<MovieResponseDto> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public MovieResponseDto create(MovieCreateDto dto) {
        return createMovieInternal(dto);
    }

    @Transactional
    public MovieResponseDto createWithReviewsTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
    }

    public MovieResponseDto createWithReviewsNonTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
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
        return MovieMapper.toResponseDto(updatedMovie);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id);
        }
        movieRepository.deleteById(id);
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
        return MovieMapper.toResponseDto(updatedMovie);
    }
}
