package com.moviecat.service;

import com.moviecat.model.Director;
import com.moviecat.repository.DirectorRepository;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final com.moviecat.repository.MovieRepository movieRepository;

    public List<Director> getAll() {
        return directorRepository.findAll();
    }

    public Director getById(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
    }

    @Transactional
    public Director create(Director director) {
        if (directorRepository.existsByFullName(director.getFullName())) {
            throw new ResourceAlreadyExistsException("Director with name '" + director.getFullName() + "' already exists");
        }
        return directorRepository.save(director);
    }

    @Transactional
    public Director update(Long id, Director directorDetails) {
        Director director = getById(id);

        if (directorRepository.existsByFullNameAndIdNot(directorDetails.getFullName(), id)) {
            throw new ResourceAlreadyExistsException("Director with name '" + directorDetails.getFullName() + "' already exists");
        }

        director.setFullName(directorDetails.getFullName());
        return directorRepository.save(director);
    }

    @Transactional
    public void delete(Long id) {
        if (!directorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Director not found with id: " + id);
        }
        if (movieRepository.existsByDirectorId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete director with existing movies");
        }
        directorRepository.deleteById(id);
    }
}
