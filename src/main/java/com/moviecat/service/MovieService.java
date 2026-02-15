package com.moviecat.service;

import com.moviecat.dto.MovieDto;
import com.moviecat.mapper.MovieMapper;
import com.moviecat.model.Director;
import com.moviecat.model.Genre;
import com.moviecat.model.Movie;
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

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final StudioRepository studioRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;

    public String uploadPoster(Long id, org.springframework.web.multipart.MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        String filename = fileStorageService.storeFile(file);
        String fileUrl = "/uploads/" + filename;
        
        movie.setPosterUrl(fileUrl);
        movieRepository.save(movie);
        
        return fileUrl;
    }

    public List<MovieDto> getAll() {
        return movieRepository.findAllWithGenres()
                .stream()
                .map(MovieMapper::toDto)
                .toList();
    }

    public MovieDto getById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Movie not found with id: " + id));
        return MovieMapper.toDto(movie);
    }

    public List<MovieDto> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieMapper::toDto)
                .toList();
    }

    @Transactional
    public MovieDto create(com.moviecat.dto.MovieCreateDto dto) {
        return createMovieInternal(dto);
    }

    @Transactional
    public MovieDto createWithReviewsTransactional(com.moviecat.dto.MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
    }

    public MovieDto createWithReviewsNonTransactional(com.moviecat.dto.MovieCreateDto dto, boolean failOnPurpose) {
        return createMovieWithReviewsInternal(dto, failOnPurpose);
    }

    private MovieDto createMovieWithReviewsInternal(com.moviecat.dto.MovieCreateDto dto, boolean failOnPurpose) {
        MovieDto createdMovie = createMovieInternal(dto);
        
        if (dto.getReviews() != null) {
            for (int i = 0; i < dto.getReviews().size(); i++) {
                com.moviecat.dto.ReviewDto reviewDto = dto.getReviews().get(i);
                com.moviecat.model.Review review = com.moviecat.mapper.ReviewMapper.toEntity(reviewDto);
                
                Movie movieEntity = movieRepository.findById(createdMovie.getId()).orElseThrow();
                review.setMovie(movieEntity);
                
                if (failOnPurpose && i == dto.getReviews().size() - 1) {
                    throw new RuntimeException("Simulated failure during review processing");
                }
                movieEntity.getReviews().add(review);
                movieRepository.save(movieEntity);
            }
        }
        
        return MovieMapper.toDto(movieRepository.findById(createdMovie.getId()).orElseThrow());
    }



    private MovieDto createMovieInternal(com.moviecat.dto.MovieCreateDto dto) {
        if (movieRepository.existsByTitle(dto.getTitle())) {
            throw new RuntimeException("Movie with title '" + dto.getTitle() + "' already exists");
        }
        Movie movie = MovieMapper.toEntity(dto);

        if (dto.getDirectorId() != null) {
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new RuntimeException("Director not found"));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new RuntimeException("Studio not found"));
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
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        if (!movie.getTitle().equals(dto.getTitle()) && movieRepository.existsByTitle(dto.getTitle())) {
            throw new RuntimeException("Movie with title '" + dto.getTitle() + "' already exists");
        }

        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setDuration(dto.getDuration());
        movie.setViewCount(dto.getViewCount());

        if (dto.getDirectorId() != null) {
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new RuntimeException("Director not found"));
            movie.setDirector(director);
        } else {
            movie.setDirector(null);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new RuntimeException("Studio not found"));
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
            throw new RuntimeException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    @Transactional
    public MovieDto patch(Long id, com.moviecat.dto.MoviePatchDto dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        MovieMapper.updateEntity(movie, dto);

        if (dto.getDirectorId() != null) {
            Director director = directorRepository.findById(dto.getDirectorId())
                    .orElseThrow(() -> new RuntimeException("Director not found"));
            movie.setDirector(director);
        }

        if (dto.getStudioId() != null) {
            Studio studio = studioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new RuntimeException("Studio not found"));
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
