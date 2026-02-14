package com.moviecat.service;

import com.moviecat.dto.MovieDto;
import com.moviecat.mapper.MovieMapper;
import com.moviecat.model.Movie;
import com.moviecat.repository.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDto> getAll() {
        return movieRepository.findAll()
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

    public MovieDto create(MovieDto dto) {
        Movie movie = MovieMapper.toEntity(dto);
        Movie savedMovie = movieRepository.save(movie);
        return MovieMapper.toDto(savedMovie);
    }
}
