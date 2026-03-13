package com.moviecat.service.cache;

import java.util.Objects;

public final class MovieSearchKey {

    private final String titleNormalized;
    private final String directorLastNameNormalized;
    private final String genreNameNormalized;
    private final String studioTitleNormalized;
    private final int page;
    private final int size;
    private final String sort;
    private final String direction;
    private final boolean nativeQuery;

    public MovieSearchKey(
            String titleNormalized,
            String directorLastNameNormalized,
            String genreNameNormalized,
            String studioTitleNormalized,
            int page,
            int size,
            String sort,
            String direction,
            boolean nativeQuery) {
        this.titleNormalized = titleNormalized;
        this.directorLastNameNormalized = directorLastNameNormalized;
        this.genreNameNormalized = genreNameNormalized;
        this.studioTitleNormalized = studioTitleNormalized;
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
        this.nativeQuery = nativeQuery;
    }

    public String getTitleNormalized() {
        return titleNormalized;
    }

    public String getDirectorLastNameNormalized() {
        return directorLastNameNormalized;
    }

    public String getGenreNameNormalized() {
        return genreNameNormalized;
    }

    public String getStudioTitleNormalized() {
        return studioTitleNormalized;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSort() {
        return sort;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isNativeQuery() {
        return nativeQuery;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MovieSearchKey other)) {
            return false;
        }
        return page == other.page
                && size == other.size
                && nativeQuery == other.nativeQuery
                && Objects.equals(titleNormalized, other.titleNormalized)
                && Objects.equals(directorLastNameNormalized, other.directorLastNameNormalized)
                && Objects.equals(genreNameNormalized, other.genreNameNormalized)
                && Objects.equals(studioTitleNormalized, other.studioTitleNormalized)
                && Objects.equals(sort, other.sort)
                && Objects.equals(direction, other.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                titleNormalized,
                directorLastNameNormalized,
                genreNameNormalized,
                studioTitleNormalized,
                page,
                size,
                sort,
                direction,
                nativeQuery);
    }
}
