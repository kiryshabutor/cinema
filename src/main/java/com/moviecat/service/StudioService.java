package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Studio;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.StudioRepository;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudioService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_DIRECTION = "asc";
    private static final String DESC_DIRECTION = "desc";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(DEFAULT_SORT_FIELD, "title", "address");

    private final StudioRepository studioRepository;
    private final MovieRepository movieRepository;
    private final MovieSearchCache movieSearchCache;

    public Page<Studio> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);
        String normalizedDirection = normalizeDirection(direction);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                buildSort(normalizedSort, normalizedDirection));
        return studioRepository.findAll(pageRequest);
    }

    public Studio getById(@NonNull Long id) {
        return studioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studio not found with id: " + id));
    }

    @Transactional
    public Studio create(Studio studio) {
        if (studioRepository.existsByTitle(studio.getTitle())) {
            throw new ResourceAlreadyExistsException("Studio with title '" + studio.getTitle() + "' already exists");
        }
        Studio savedStudio = studioRepository.save(studio);
        movieSearchCache.invalidate("StudioService.create");
        return savedStudio;
    }

    @Transactional
    public Studio update(@NonNull Long id, Studio studioDetails) {
        Studio studio = getById(id);

        if (studioRepository.existsByTitleAndIdNot(studioDetails.getTitle(), id)) {
            throw new ResourceAlreadyExistsException(
                    "Studio with title '" + studioDetails.getTitle() + "' already exists");
        }

        studio.setTitle(studioDetails.getTitle());
        studio.setAddress(studioDetails.getAddress());
        Studio updatedStudio = studioRepository.save(studio);
        movieSearchCache.invalidate("StudioService.update id=" + id);
        return updatedStudio;
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!studioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Studio not found with id: " + id);
        }
        if (movieRepository.existsByStudioId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete studio with existing movies");
        }
        studioRepository.deleteById(id);
        movieSearchCache.invalidate("StudioService.delete id=" + id);
    }

    private int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String normalizeSort(String sort) {
        if (sort == null) {
            return DEFAULT_SORT_FIELD;
        }
        String trimmedSort = sort.trim();
        if (trimmedSort.isEmpty()) {
            return DEFAULT_SORT_FIELD;
        }
        if (!ALLOWED_SORT_FIELDS.contains(trimmedSort)) {
            return DEFAULT_SORT_FIELD;
        }
        return trimmedSort;
    }

    private String normalizeDirection(String direction) {
        if (direction == null) {
            return DEFAULT_DIRECTION;
        }
        String normalizedDirection = direction.trim().toLowerCase(Locale.ROOT);
        if (!DESC_DIRECTION.equals(normalizedDirection) && !DEFAULT_DIRECTION.equals(normalizedDirection)) {
            return DEFAULT_DIRECTION;
        }
        return normalizedDirection;
    }

    private Sort buildSort(String sort, String direction) {
        if (DESC_DIRECTION.equals(direction)) {
            return Sort.by(sort).descending();
        }
        return Sort.by(sort).ascending();
    }
}
