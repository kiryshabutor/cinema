package com.moviecat.service;

import com.moviecat.model.Studio;
import com.moviecat.repository.StudioRepository;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudioService {

    private final StudioRepository studioRepository;
    private final com.moviecat.repository.MovieRepository movieRepository;

    public List<Studio> getAll() {
        return studioRepository.findAll();
    }

    public Studio getById(Long id) {
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
    public Studio update(Long id, Studio studioDetails) {
        Studio studio = getById(id);

        if (studioRepository.existsByTitleAndIdNot(studioDetails.getTitle(), id)) {
            throw new ResourceAlreadyExistsException(
                    "Studio with title '" + studioDetails.getTitle() + "' already exists");
        }

        studio.setTitle(studioDetails.getTitle());
        studio.setAddress(studioDetails.getAddress());
        return studioRepository.save(studio);
    }

    @Transactional
    public void delete(Long id) {
        if (!studioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Studio not found with id: " + id);
        }
        if (movieRepository.existsByStudioId(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete studio with existing movies");
        }
        studioRepository.deleteById(id);
    }
}
