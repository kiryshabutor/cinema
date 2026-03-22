package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Studio;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.StudioRepository;
import com.moviecat.service.cache.MovieByIdCache;
import com.moviecat.service.cache.MovieSearchCache;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final MovieByIdCache movieByIdCache;
    private final MovieSearchCache movieSearchCache;

    public Page<Studio> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = PagingSortingUtils.normalizePage(page, DEFAULT_PAGE);
        int normalizedSize = PagingSortingUtils.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
        String normalizedSort = PagingSortingUtils.normalizeSort(
                sort, DEFAULT_SORT_FIELD, ALLOWED_SORT_FIELDS, false);
        String normalizedDirection = PagingSortingUtils.normalizeDirection(
                direction, DEFAULT_DIRECTION, DESC_DIRECTION);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                PagingSortingUtils.buildSort(normalizedSort, normalizedDirection, DESC_DIRECTION));
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
        return studioRepository.save(studio);
    }

    @Transactional
    public Studio update(@NonNull Long id, Studio studioDetails) {
        Studio studio = getById(id);

        if (studioRepository.existsByTitleAndIdNot(studioDetails.getTitle(), id)) {
            throw new ResourceAlreadyExistsException(
                    "Studio with title '" + studioDetails.getTitle() + "' already exists");
        }

        List<Long> relatedMovieIds = movieRepository.findIdsByStudioId(id);
        studio.setTitle(studioDetails.getTitle());
        studio.setAddress(studioDetails.getAddress());
        Studio updatedStudio = studioRepository.save(studio);
        movieSearchCache.invalidate("StudioService.update id=" + id);
        movieByIdCache.evictAll(relatedMovieIds, "StudioService.update id=" + id);
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
    }

}
