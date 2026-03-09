package com.moviecat.mapper;

import com.moviecat.dto.GenreItemDto;
import com.moviecat.dto.MovieCreateDto;
import com.moviecat.dto.MoviePatchDto;
import com.moviecat.dto.MovieResponseDto;
import com.moviecat.model.Genre;
import com.moviecat.model.Movie;
import java.util.Comparator;
import java.util.List;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static MovieResponseDto toResponseDto(Movie movie) {
        MovieResponseDto dto = new MovieResponseDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setYear(movie.getYear());
        dto.setDuration(movie.getDuration());
        dto.setViewCount(movie.getViewCount());
        dto.setPosterUrl(movie.getPosterUrl());

        if (movie.getDirector() != null) {
            dto.setDirectorId(movie.getDirector().getId());
            dto.setDirectorLastName(movie.getDirector().getLastName());
            dto.setDirectorFirstName(movie.getDirector().getFirstName());
            dto.setDirectorMiddleName(movie.getDirector().getMiddleName());
        }

        if (movie.getStudio() != null) {
            dto.setStudioId(movie.getStudio().getId());
            dto.setStudioTitle(movie.getStudio().getTitle());
        }

        if (movie.getGenres() != null) {
            List<GenreItemDto> genres = movie.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .map(genre -> new GenreItemDto(genre.getId(), genre.getName()))
                    .toList();
            dto.setGenres(genres);
        }

        return dto;
    }

    public static Movie toEntity(MovieCreateDto dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setDuration(dto.getDuration());
        movie.setViewCount(dto.getViewCount());
        return movie;
    }

    public static void updateEntity(Movie movie, MoviePatchDto dto) {
        if (dto.getTitle() != null) {
            movie.setTitle(dto.getTitle());
        }
        if (dto.getYear() != null) {
            movie.setYear(dto.getYear());
        }
        if (dto.getDuration() != null) {
            movie.setDuration(dto.getDuration());
        }
        if (dto.getViewCount() != null) {
            movie.setViewCount(dto.getViewCount());
        }
    }
}
