package com.moviecat.mapper;

import com.moviecat.dto.MovieDto;
import com.moviecat.model.Movie;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static MovieDto toDto(Movie movie) {
        MovieDto dto = new MovieDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setYear(movie.getYear());
        dto.setDuration(movie.getDuration());
        dto.setViewCount(movie.getViewCount());
        return dto;
    }

    public static Movie toEntity(MovieDto dto) {
        Movie movie = new Movie();
        movie.setId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setDuration(dto.getDuration());
        movie.setViewCount(dto.getViewCount());
        return movie;
    }
}
