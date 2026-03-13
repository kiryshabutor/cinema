package com.moviecat.service;

import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

final class PagingSortingUtils {

    private PagingSortingUtils() {
    }

    static int normalizePage(int page, int defaultPage) {
        return Math.max(page, defaultPage);
    }

    static int normalizeSize(int size, int defaultSize, int maxSize) {
        if (size <= 0) {
            return defaultSize;
        }
        return Math.min(size, maxSize);
    }

    static String normalizeSort(
            String sort,
            String defaultSortField,
            Set<String> allowedSortFields,
            boolean ignoreCase) {
        if (sort == null) {
            return defaultSortField;
        }
        String trimmedSort = sort.trim();
        if (trimmedSort.isEmpty()) {
            return defaultSortField;
        }
        if (!ignoreCase) {
            if (!allowedSortFields.contains(trimmedSort)) {
                return defaultSortField;
            }
            return trimmedSort;
        }
        for (String allowedField : allowedSortFields) {
            if (allowedField.equalsIgnoreCase(trimmedSort)) {
                return allowedField;
            }
        }
        return defaultSortField;
    }

    static String normalizeDirection(String direction, String defaultDirection, String descDirection) {
        if (direction == null) {
            return defaultDirection;
        }
        String normalizedDirection = direction.trim().toLowerCase(Locale.ROOT);
        if (!descDirection.equals(normalizedDirection) && !defaultDirection.equals(normalizedDirection)) {
            return defaultDirection;
        }
        return normalizedDirection;
    }

    static @NonNull Sort buildSort(String sortField, String sortDirection, String descDirection) {
        if (descDirection.equals(sortDirection)) {
            return Sort.by(sortField).descending();
        }
        return Sort.by(sortField).ascending();
    }
}
