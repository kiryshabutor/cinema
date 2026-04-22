package com.moviecat.service.cache;

public record MovieSearchKey(
        String titleNormalized,
        String directorLastNameNormalized,
        String directorFirstNameNormalized,
        String genreNameNormalized,
        String studioTitleNormalized,
        PagingOptions pagingOptions,
        boolean nativeQuery) {

    public record PagingOptions(int page, int size, String sort, String direction) {
    }
}
