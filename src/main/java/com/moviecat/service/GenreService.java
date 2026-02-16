package com.moviecat.service;

import com.moviecat.exception.ResourceAlreadyExistsException;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Genre;
import com.moviecat.repository.GenreRepository;
import com.moviecat.repository.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public Genre getById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    @Transactional
    public Genre create(Genre genre) {
        if (genreRepository.existsByName(genre.getName())) {
            throw new ResourceAlreadyExistsException("Genre with name '" + genre.getName() + "' already exists");
        }
        return genreRepository.save(genre);
    }

    @Transactional
    public Genre update(Long id, Genre genreDetails) {
        Genre genre = getById(id);

        if (genreRepository.existsByNameAndIdNot(genreDetails.getName(), id)) {
            throw new ResourceAlreadyExistsException("Genre with name '" + genreDetails.getName() + "' already exists");
        }

        genre.setName(genreDetails.getName());
        return genreRepository.save(genre);
    }

    @Transactional
    public void delete(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre not found with id: " + id);
        }
        if (movieRepository.existsByGenresId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete genre with existing movies");
        }
        genreRepository.deleteById(id);
    }
}
