package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieSearchParams {
    private final String title;
    private final String directorLastName;
    private final String genreName;
    private final String studioTitle;
    private final int page;
    private final int size;
    private final String sort;
    private final String direction;
    private final boolean nativeQuery;
}
