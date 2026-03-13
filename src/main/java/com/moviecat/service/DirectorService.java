package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Director;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.repository.MovieRepository;
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
public class DirectorService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_DIRECTION = "asc";
    private static final String DESC_DIRECTION = "desc";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "lastName", "firstName", "middleName");

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;
    private final MovieSearchCache movieSearchCache;

    public Page<Director> getAll(int page, int size, String sort, String direction) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);
        String normalizedDirection = normalizeDirection(direction);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                buildSort(normalizedSort, normalizedDirection));
        return directorRepository.findAll(pageRequest);
    }

    public Director getById(@NonNull Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
    }

    @Transactional
    public Director create(Director director) {
        String lastName = normalizeRequiredName(director.getLastName());
        String firstName = normalizeRequiredName(director.getFirstName());
        String middleName = normalizeMiddleName(director.getMiddleName());

        if (directorRepository.existsByLastNameAndFirstNameAndMiddleName(lastName, firstName, middleName)) {
            throw new ResourceAlreadyExistsException(
                    "Director with name '" + formatFullName(lastName, firstName, middleName) + "' already exists");
        }

        director.setLastName(lastName);
        director.setFirstName(firstName);
        director.setMiddleName(middleName);
        Director savedDirector = directorRepository.save(director);
        movieSearchCache.invalidate("DirectorService.create");
        return savedDirector;
    }

    @Transactional
    public Director update(@NonNull Long id, Director directorDetails) {
        Director director = getById(id);
        String lastName = normalizeRequiredName(directorDetails.getLastName());
        String firstName = normalizeRequiredName(directorDetails.getFirstName());
        String middleName = normalizeMiddleName(directorDetails.getMiddleName());

        if (directorRepository.existsByLastNameAndFirstNameAndMiddleNameAndIdNot(
                lastName, firstName, middleName, id)) {
            throw new ResourceAlreadyExistsException(
                    "Director with name '" + formatFullName(lastName, firstName, middleName) + "' already exists");
        }

        director.setLastName(lastName);
        director.setFirstName(firstName);
        director.setMiddleName(middleName);
        Director updatedDirector = directorRepository.save(director);
        movieSearchCache.invalidate("DirectorService.update id=" + id);
        return updatedDirector;
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!directorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Director not found with id: " + id);
        }
        if (movieRepository.existsByDirectorId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete director with existing movies");
        }
        directorRepository.deleteById(id);
        movieSearchCache.invalidate("DirectorService.delete id=" + id);
    }

    private String normalizeRequiredName(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeMiddleName(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatFullName(String lastName, String firstName, String middleName) {
        if (middleName == null) {
            return lastName + " " + firstName;
        }
        return lastName + " " + firstName + " " + middleName;
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
