package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Director;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.repository.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    public List<Director> getAll() {
        return directorRepository.findAll();
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
        return directorRepository.save(director);
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
        return directorRepository.save(director);
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
}
