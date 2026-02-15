package com.moviecat.mapper;

import com.moviecat.dto.MovieCreateDto;
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
        dto.setPosterUrl(movie.getPosterUrl());

        if (movie.getDirector() != null) {
            dto.setDirectorId(movie.getDirector().getId());
            dto.setDirectorName(movie.getDirector().getFullName());
        }

        if (movie.getStudio() != null) {
            dto.setStudioId(movie.getStudio().getId());
            dto.setStudioTitle(movie.getStudio().getTitle());
        }

        if (movie.getGenres() != null) {
            java.util.Set<Long> genreIds = new java.util.HashSet<>();
            java.util.Set<String> genreNames = new java.util.HashSet<>();
            for (com.moviecat.model.Genre genre : movie.getGenres()) {
                genreIds.add(genre.getId());
                genreNames.add(genre.getName());
            }
            dto.setGenreIds(genreIds);
            dto.setGenreNames(genreNames);
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

    public static Movie toEntity(MovieDto dto) {
        Movie movie = new Movie();
        movie.setId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setYear(dto.getYear());
        movie.setDuration(dto.getDuration());
        movie.setViewCount(dto.getViewCount());
        movie.setPosterUrl(dto.getPosterUrl());
        return movie;
    }
    
    public static void updateEntity(Movie movie, com.moviecat.dto.MoviePatchDto dto) {
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
