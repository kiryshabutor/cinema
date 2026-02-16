package com.moviecat.service;

import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MovieDto;
import com.moviecat.dto.MoviePatchDto;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
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

    public String uploadPoster(Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        String filename = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + filename;
        
        movie.setPosterUrl(fileUrl);
        movieRepository.save(movie);
        
        return fileUrl;
    }

    public List<MovieDto> getAll(String fetchType) {
        List<Movie> movies;
        if ("lazy".equalsIgnoreCase(fetchType)) {
            movies = movieRepository.findAll();
        } else {
            movies = movieRepository.findAllWithDetails();
        }
        return movies.stream()
                .map(MovieMapper::toDto)
                .toList();
    }

    public MovieDto getById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MOVIE_NOT_FOUND_MSG + id));
        return MovieMapper.toDto(movie);
    }

    public List<MovieDto> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieMapper::toDto)
                .toList();
    }

    @Transactional
    public MovieDto create(MovieCreateDto dto) {
        return createMovieInternal(dto);
    }

    @Transactional
    public MovieDto createWithReviewsTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
    }

    public MovieDto createWithReviewsNonTransactional(MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
    }

    private MovieDto createMovieWithReviewsInternal(MovieCreateDto dto, boolean failOnPurpose) {
        MovieDto createdMovie = createMovieInternal(dto);
        
        if (dto.getReviews() != null) {
            for (int i = 0; i < dto.getReviews().size(); i++) {
                ReviewDto reviewDto = dto.getReviews().get(i);
                Review review = ReviewMapper.toEntity(reviewDto);
                
                Movie movieEntity = movieRepository.findById(createdMovie.getId()).orElseThrow();
                review.setMovie(movieEntity);
                
                if (failOnPurpose && i == dto.getReviews().size() - 1) {
                    throw new SimulatedFailureException("Simulated failure during review processing");
                }
                movieEntity.getReviews().add(review);
                movieRepository.save(movieEntity);
            }
        }
        
        return MovieMapper.toDto(movieRepository.findById(createdMovie.getId()).orElseThrow());
    }

    private MovieDto createMovieInternal(MovieCreateDto dto) {
        if (movieRepository.existsByTitle(dto.getTitle())) {
            throw new ResourceAlreadyExistsException("Movie with title '" + dto.getTitle() + "' already exists");
        }
        Movie movie = MovieMapper.toEntity(dto);

        if (dto.getDirectorId() != null) {
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
            movie.setGenres(genres);
        }

        Movie savedMovie = movieRepository.save(movie);
        return MovieMapper.toDto(savedMovie);
    }

    @Transactional
    public MovieDto update(Long id, MovieDto dto) {
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
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        } else {
            movie.setDirector(null);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        } else {
            movie.setStudio(null);
        }

        if (dto.getGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
            movie.setGenres(genres);
        } else {
            movie.getGenres().clear();
        }

        Movie updatedMovie = movieRepository.save(movie);
        return MovieMapper.toDto(updatedMovie);
    }

    @Transactional
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id);
        }
        movieRepository.deleteById(id);
    }

    @Transactional
    public MovieDto patch(Long id, MoviePatchDto dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MOVIE_NOT_FOUND_MSG + id));

        MovieMapper.updateEntity(movie, dto);

        if (dto.getDirectorId() != null) {
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException(DIRECTOR_NOT_FOUND_MSG));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new ResourceNotFoundException(STUDIO_NOT_FOUND_MSG));
            movie.setStudio(studio);
        }

        if (dto.getGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
            movie.setGenres(genres);
        }

        Movie updatedMovie = movieRepository.save(movie);
        return MovieMapper.toDto(updatedMovie);
    }
}
