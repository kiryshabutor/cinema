package com.moviecat.dto;

public record MovieSearchCriteria(
    String title,
    String directorName,
    String studioTitle,
    String genreName,
    Integer page,
    Integer size
) {}
